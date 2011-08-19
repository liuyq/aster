#
# Copyright (C) 2011 0xlab - http://0xlab.org/
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Authored by Wei-Ning Huang <azhuang@0xlab.org>
#

from org.zeroxlab.wookieerunner import WookieeRunner

DEVICE = None

def connect(*args, **kwargs):
    global DEVICE
    print 'Wating for connection...'
    DEVICE = WookieeRunner.waitForConnection(*args, **kwargs)
    print 'Connection established.'


def takeSnapshot(name):
    if not name.endswith('.png'):
        name += '.png'
    DEVICE.takeSnapshot().writeToFile(name, 'png')


def getProperty(*args, **kwargs):
    return DEVICE.getProperty(*args, **kwargs)


def getSystemProperty(*args, **kwargs):
    return DEVICE.getSystemProperty(*args, **kwargs)


def touch(*args, **kwargs):
    return DEVICE.touch(*args, **kwargs)


def drag(*args, **kwargs):
    return DEVICE.drag(*args, **kwargs)


def press(*args, **kwargs):
    return DEVICE.press(*args, **kwargs)


def typet(*args, **kwargs):
    return DEVICE.type(*args, **kwargs)


def wait(*arg, **kwargs):
    if type(arg[0]) == str:
        return DEVICE.wait(*arg, **kwargs)
    else:
        return sleep(*arg, **kwargs);


def shell(*args, **kwargs):
    return DEVICE.shell(*args, **kwargs)


def reboot(*args, **kwargs):
    return DEVICE.reboot(*args, **kwargs)


def push(*args, **kwargs):
    return DEVICE.push(*args, **kwargs)


def pull(*args, **kwargs):
    return DEVICE.pull(*args, **kwargs)


def installPackage(*args, **kwargs):
    return DEVICE.installPackage(*args, **kwargs)


def removePackage(*args, **kwargs):
    return DEVICE.removePackage(*args, **kwargs)


def startActivity(*args, **kwargs):
    return DEVICE.startActivity(*args, **kwargs)


def braodcastIntent(*args, **kwargs):
    return DEVICE.braodcastIntent(*args, **kwargs)


def instrument(*args, **kwargs):
    return DEVICE.instrument(*args, **kwargs)


def wake():
    return DEVICE.wake()


def sleep(seconds):
    WookieeRunner.sleep(seconds)
