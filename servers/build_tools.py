#!/usr/bin/env python
#
# Copyright 2018 - The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the',  help="License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an',  help="AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import logging
import argparse
import os
import platform
import sys
import subprocess

from Queue import Queue
from threading import Thread


AOSP_ROOT = os.path.abspath(os.path.join(
    os.path.dirname(os.path.realpath(__file__)), '..', '..', '..'))
TOOLS = os.path.join(AOSP_ROOT, 'tools')


def _reader(pipe, queue):
    try:
        with pipe:
            for line in iter(pipe.readline, b''):
                queue.put((pipe, line[:-1]))
    finally:
        queue.put(None)


def log_std_out(proc):
    """Logs the output of the given process."""
    q = Queue()
    Thread(target=_reader, args=[proc.stdout, q]).start()
    Thread(target=_reader, args=[proc.stderr, q]).start()
    for _ in range(2):
        for _, line in iter(q.get, None):
            logging.info(line)


def run(cmd, env):
    logging.info(' '.join(cmd))
    cmd_env = os.environ.copy()
    cmd_env.update(env)
    is_windows = (platform.system() == 'Windows')

    proc = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        shell=is_windows, # Make sure windows propagates ENV vars properly.
        cwd=AOSP_ROOT,
        env=cmd_env)

    log_std_out(proc)
    proc.wait()
    if proc.returncode != 0:
        raise Exception('Failed to run %s - %s' %
                        (' '.join(cmd), proc.returncode))


def install_deps():
    run(['python', os.path.join(AOSP_ROOT, 'external', 'qemu', 'android',
                                'build', 'python', 'setup.py'), 'develop', '--user'], {})


def main(argv):
    logging.basicConfig(format='%(message)s', level=logging.INFO)
    parser = argparse.ArgumentParser(
        description='Configures the android emulator cmake project so it can be build')
    parser.add_argument("--out_dir", type=str, required=True,
                        help="The ouput directory")
    parser.add_argument("--dist_dir", type=str, required=True,
                        help="The destination directory")
    parser.add_argument("--build-id", type=str, default=[], required=True,
                        dest='build_id',  help="The emulator build number")
    parser.add_argument("--target", type=str, default=platform.system(),
                        help="The build target, defaults to current os")

    args = parser.parse_args()
    logging.info("Building on %s - %s", platform.system(), platform.uname())

    ext = ''
    if platform.system() == 'Windows':
        ext = '.bat'


    mingw = False
    sdk = 'makeSdk'
    if args.target == 'Windows':
        sdk = 'makeWinSdk'

    if args.target == 'Mingw':
        sdk = 'makeWinSdk'
        mingw = True



    if not os.path.isabs(args.out_dir):
        args.out_dir = os.path.join(AOSP_ROOT, args.out_dir)

    env = {
        "OUT_DIR": args.out_dir,
        "DIST_DIR": args.dist_dir,
        "BUILD_NUMBER": args.build_id,
        "MINGW" : "%s" % mingw
    }

    gradle = os.path.join(TOOLS, 'gradlew%s' % ext)
    build_gradle = os.path.join(TOOLS, 'build.gradle')

    # Make sure we have all the build dependencies
    install_deps()


    logging.info("Preparing gradle.")
    run([gradle, '-b', build_gradle, '--no-daemon', '--info', 'publishLocal'], env)

    logging.info("Starting actual build.")
    run([gradle, '-b', build_gradle, '--no-daemon', '--info', 'dist', sdk], env)


if __name__ == '__main__':
    main(sys.argv)
