## IMPORTANT: Archived due to inactivity - no longer supported

[![alt Download Latest Release](https://img.shields.io/badge/download-v0.1.12-blue.svg)](https://github.com/wave2/filerecorder/releases/download/v0.1.12/fileRecorder-v0.1.12.jar) [![GitHub license](https://img.shields.io/badge/license-BSD-blue.svg)](https://raw.githubusercontent.com/wave2/filerecorder/master/LICENSE) [![Build Status](https://travis-ci.org/wave2/filerecorder.svg?branch=master)](https://travis-ci.org/wave2/filerecorder) [![GitHub issues](https://img.shields.io/github/issues/wave2/filerecorder.svg)](https://github.com/wave2/filerecorder/issues)
# fileRecorder
![alt fileRecorder Logo](https://raw.githubusercontent.com/wave2/filerecorder/gh-pages/images/fileRecorderLogo.jpg)

## Introduction
fileRecorder is a simple wrapper over [GIT](https://git-scm.com/) to allow you to track changes to a folder.

You provide 2 folders:

1. A folder to monitor
2. A folder to record the changes (git repo)

Each time you run the app, the recorder will compare your folder with the repo, and commit any changes it detects - how lazy is that?!

## Requirements
 * Java 1.8+ *(Stay current for your own sake)*

## Usage
### Recording
Pretty simple really...or you'd be using Git right?
```bash
$ java -jar fileRecorder-v0.1.12.jar -f PATH -r PATH record
```
### Rewinding
Too lazy to use Git? ;)

Alright, well just run the following commands to get a Zip file containing your files (hope it's not massive!).

 1. Get a list of the changes and make a note of the commit ID you want to get

    ```bash
    $ java -jar fileRecorder.v0.1.12.jar -f MONITOR_PATH -r REPO_PATH rewind
    ```
    *Hint: it looks like this - 5bdc5da*
 2. Create a Zip file containing a point in time snapshot of the specified commit ID.

    ```bash
    $ java -jar fileRecorder.v0.1.12.jar -f MONITOR_PATH -r REPO_PATH rewind [commitID]
    ```

You'll have to restore them yourself after that, hmmm that sounds like a feature request.

## License
Copyright (c) 2016 Wave2 Limited
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of Wave2 Limited.
