
from distutils.spawn import find_executable
import itertools
import logging
import os
import platform
import subprocess
try:
    import _winreg as winreg
except:
    # Winreg is a windows only thing..
    pass


def disable_debug_policy():
    try:
        with winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\Policies\Microsoft\Windows\Windows Error Reporting") as registry_key:
            winreg.SetValue(registry_key, "DontShowUI", 1)
    except:
        logging.error("Failed to retrieve, set status.")

    # Next clear out the just in time debuggers.
    todelete = [(r"SOFTWARE\Microsoft\Windows NT\CurrentVersion\AeDebug", "Debugger"),
                (r"SOFTWARE\Wow6432Node\Microsoft\Windows NT\CurrentVersion\AeDebug", "Debugger")]
    for current_key, entry in todelete:
        try:
            # See https://docs.microsoft.com/en-us/visualstudio/debugger/debug-using-the-just-in-time-debugger?view=vs-2019#disable-just-in-time-debugging-from-the-windows-registry)
            with winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, current_key, 0, winreg.KEY_ALL_ACCESS) as open_key:
                winreg.DeleteValue(open_key, entry)
        except:
            pass


# A class that is responsible for configuring the server when running the build.
class ServerConfig(object):

    def __init__(self, presubmit):
        self.presubmit = presubmit
        self.env = os.environ.copy()

    def get_env(self):
        return self.env

    def __enter__(self):

        # On windows we do not want debug ui to be activated.
        if platform.system() == 'Windows':
            disable_debug_policy()

        # Never run ccache outside of presubmit, even if it might be available.
        if not self.presubmit:
            self.env['CCACHE_DISABLE'] = 'True'
        else:
            # We cannot rely on mtime for compiler identification as the build bots
            # do a fresh checkout of the compiler.
            self.env['CCACHE_COMPILERCHECK'] = 'string:%compiler% --version'
        return self

    def __exit__(self, exc_type, exc_value, tb):
        # We clear the cache in case of failures.
        if exc_type and exc_value:
            ccache = find_executable('ccache')
            if ccache:
                subprocess.call([ccache, '-C'])
