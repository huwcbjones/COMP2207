#!/bin/bash
ping -n 60 www.google.co.uk | sed -e 's/PING.*//g' -e 's/.*time=//g' -e 's/\sms//g'
