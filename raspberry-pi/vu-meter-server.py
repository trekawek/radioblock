#!/usr/bin/env python3

import argparse
import binascii
import fcntl
import logging
import os
import random
import signal
import socket
import struct
import sys
import time

from rpi_ws281x import Color, Adafruit_NeoPixel

LED_COUNT      = 32      # Number of LED pixels.
LED_PIN        = 18      # GPIO pin connected to the pixels (must support PWM!).
LED_FREQ_HZ    = 800000  # LED signal frequency in hertz (usually 800khz)
LED_DMA        = 5       # DMA channel to use for generating signal (try 5)
LED_BRIGHTNESS = 10      # Set to 0 for darkest and 255 for brightest
LED_INVERT     = False   # True to invert the signal (when using NPN transistor level shift)

LEVELS_MAX     = 75
SPECTRUM_MAX   = 50

VO_COLORS = (
    Color(0, 255, 0),
    Color(64, 255, 0),
    Color(96, 255, 0),
    Color(128, 255, 0),
    Color(255, 255, 0),
    Color(255, 128, 0),
    Color(255, 64, 0),
    Color(255, 0, 0)
)

SPECTRUM_COLORS = (
    Color(0, 255, 0),
    Color(128, 255, 0),
    Color(255, 255, 0),
    Color(255, 0, 0)
)

class LedDemo:
    def __init__(self, strip):
        self.strip = strip
        self.index = 1
        self.demos = ['demo1', 'demo2']

    def demo(self):
        getattr(self, self.demos[self.index])()
        self.index += 1
        self.index %= len(self.demos)

    def demo1(self):
        pattern = """B__BO__O
                     _BB__OO_
                     _BB__OO_
                     B__BO__O"""
        colors = { "B": Color(0, 0, 255), "O": Color(255, 165, 0) }
        self.display(pattern, colors)

    def demo2(self):
        pattern = """12343567
                     8_______
                     4_______
                     8484848_"""
        colors = { "1": Color(165, 42, 42),    # brown
                   "2": Color(255, 192, 203),  # pink
                   "3": Color(0,0,255),        # blue
                   "4": Color(0,255,0),        # green
                   "5": Color(128, 128, 128),  # grey
                   "6": Color(255, 165, 0),    # orange
                   "7": Color(128, 0, 128),    # purple
                   "8": Color(255, 0, 0),      # red
                 }
        self.display(pattern, colors)

    def display(self, pattern, colors):
        i = 0
        colors['_'] = Color(0, 0, 0)
        for c in pattern.replace(" ", ""):
            if (c in colors):
                self.strip.setPixelColor(i, colors[c])
            else:
                continue
            i += 1
        self.strip.show()

class FifoReader:
    def __init__(self, path, fmt, length):
        self.fifo = None
        self.path = path
        self.fmt = fmt
        self.length = length

    def open(self):
        self.close()
        try:
            self.fifo = open(self.path, "rb")
            fd = self.fifo.fileno()
            flag = fcntl.fcntl(fd, fcntl.F_GETFD)
            fcntl.fcntl(fd, fcntl.F_SETFL, flag | os.O_NONBLOCK)
            return True
        except Exception as e:
            logging.error(e)
            return False

    def close(self):
        if (self.fifo):
            try:
                self.fifo.close()
            except:
                pass
            self.fifo = None

    def read(self):
        if (not self.fifo):
            if (not self.open()):
                return None
        try:
            data = self.fifo.read(self.length)
            if (data == None or len(data) < self.length):
                return None
            return struct.unpack(self.fmt, data)
        except Exception as e:
            logging.error(e)
            self.close()
            return

class LedVO:
    def __init__(self, display_type):
        self.strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS)
        self.display_type = display_type
        self.do_stop = False

        self.radioblock_fifo = FifoReader("/home/pi/fifo/radioblock.fifo", "<hb", 3)

        self.is_muted = False
        self.is_muted_shown = False

        self.lvl_fifo = FifoReader("/home/pi/fifo/lvl.fifo", "<hh", 4)
        self.sa_fifo = FifoReader("/home/pi/fifo/sa.fifo", "<IIIIIIII", 32)
        
        self.last_update = 0
        self.led_demo = LedDemo(self.strip)

        random.seed()

    def setup(self):
        self.strip.begin()
        for i in range(0, LED_COUNT):
            self.strip.setPixelColor(i, Color(0, 0, 0))
        self.strip.show()

    def stop(self):
        self.do_stop = True
        for i in range(0, LED_COUNT):
            self.strip.setPixelColor(i, Color(0, 0, 0))
        self.strip.show()

        self.radioblock_fifo.close()
        self.lvl_fifo.close()
        self.sa_fifo.close()

    def loop(self):
        while not self.do_stop:
            now = time.time()

            radioblock = self.radioblock_fifo.read()

            if (radioblock):
                self.is_muted = (radioblock[1] == 0)
                if (not self.is_muted):
                    self.is_muted_shown = False

            levels = self.lvl_fifo.read()
            spectrum = self.sa_fifo.read()
           
            if (now - self.last_update > (1 / 60)):
                if (self.is_muted):
                    if (not self.is_muted_shown):
                        self.show_muted()
                        self.is_muted_shown = True
                elif (self.display_type == 'spectrum_horizontal' and spectrum):
                    self.show_spectrum_horizontal(spectrum)
                elif (self.display_type == 'spectrum_vertical' and spectrum):
                    self.show_spectrum_vertical(spectrum)
                elif (self.display_type == 'levels' and levels):
                    self.show_level(levels)
                self.last_update = now

            time.sleep(1 / 60)

    def show_level(self, levels):
        m = levels[0] * 8 / LEVELS_MAX
        for i in range(0, 8):
            if (i < m):
                c = VO_COLORS[i]
            else:
                c = Color(0, 0, 0)
            for j in range(0, 4):
                self.strip.setPixelColor(i + j * 8, c)
        self.strip.show()
    
    def show_spectrum_horizontal(self, spectrum):
        for i in range(0, 4):
            m = (spectrum[i * 2] + spectrum[i * 2 + 1]) * 8 / SPECTRUM_MAX
            for j in range(0, 8):
                if (j < m):
                    c = VO_COLORS[j]
                else:
                    c = Color(0, 0, 0)    
                self.strip.setPixelColor(i * 8 + j, c)
        self.strip.show()

    def show_spectrum_vertical(self, spectrum):
        for i in range(0, 8):
            m = spectrum[i] * 4 / SPECTRUM_MAX
            for j in range(0, 4):
                if (j < m):
                    c = SPECTRUM_COLORS[j]
                else:
                    c = Color(0, 0, 0)    
                self.strip.setPixelColor(i + (3 - j) * 8, c)
        self.strip.show()

    def show_muted(self):
        self.led_demo.demo()

if len(sys.argv) > 1:
    t = sys.argv[1]
else:
    t = 'levels'

led_vo = LedVO(t)
led_vo.setup()

def signal_handler(sig, frame):
    led_vo.stop()
    sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

led_vo.loop()
