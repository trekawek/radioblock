## Hardware components

1. Raspberry Pi
2. A cheap USB sound card, eg. [UNITEK Y-247A](https://www.x-kom.pl/p/329954-karta-dzwiekowa-unitek-y-247a-usb-stereo.html)
3. [RGB LED Hat](https://botland.com.pl/pl/raspberry-pi-hat-klawiatury-i-wyswietlacze/8869-rgb-led-hat-nakladka-do-raspberry-pi-32zero.html)

## Software setup on Pi

1. Install [peppyalsa](https://github.com/project-owner/peppyalsa.doc/wiki/Installation) and OpenJDK.
2. Copy files:
  - [asound.conf](asound.conf) -> `/etc/asound.conf`
  - [radioblock.service](radioblock.service) -> `/etc/systemd/system/radioblock.service`
  - [run-radioblock.sh](run-radioblock.sh) and [vu-meter-server.py](vu-meter-server.py) -> `/home/pi`
  - [../standalone](standalone-*.jar) -> `/home/pi`
3. Start run-radioblock.sh
4. To run it on startup:
```
systemctl enable radioblock
```
