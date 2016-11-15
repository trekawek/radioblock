#!/bin/bash

HOST=192.168.0.10

post() {
  local data="$1"
  curl -s -d "$data" "$HOST/YamahaRemoteControl/ctrl"
}

set_volume() {
  local volume="$1"
  post '<?xml version="1.0" encoding="utf-8"?><YAMAHA_AV cmd="PUT"><Main_Zone><Volume><Lvl><Val>'$volume'</Val><Exp>0</Exp><Unit></Unit></Lvl></Volume></Main_Zone></YAMAHA_AV>' > /dev/null
}

get_volume() {
  local xml=$(post '<?xml version="1.0" encoding="utf-8"?><YAMAHA_AV cmd="GET"><Main_Zone><Volume><Lvl>GetParam</Lvl></Volume></Main_Zone></YAMAHA_AV>')
  [[ "$xml" =~ \>([0-9]+) ]]
  echo ${BASH_REMATCH[1]}
}

is_radio() {
  local basic_status=$(post '<?xml version="1.0" encoding="utf-8"?><YAMAHA_AV cmd="GET"><Main_Zone><Basic_Status>GetParam</Basic_Status></Main_Zone></YAMAHA_AV>')
  [[ "$basic_status" == *"<Power>On</Power>"* ]] || return 0
  [[ "$basic_status" == *"<Input_Sel>TUNER</Input_Sel>"* ]] || return 0

  local tuner_status=$(post '<?xml version="1.0" encoding="utf-8"?><YAMAHA_AV cmd="GET"><Tuner><Play_Info>GetParam</Play_Info></Tuner></YAMAHA_AV>')
  [[ "$tuner_status" == *"8950"* ]] || return 0
  return 1
}

case "$1" in
volume)
  if [ -z "$2" ]; then
    get_volume
  else
    set_volume "$2"
  fi
;;
is_radio)
  is_radio
  echo $?
;;
esac

