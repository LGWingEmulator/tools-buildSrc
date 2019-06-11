import logging
import time
import datetime

class TimeFormatter(logging.Formatter):
    """A formatter used by the build system that:

     - Strips whitespace.
     - Formats time since start
    """

    def __init__(self, fmt=None):
        self.start_time = time.time()
        super(TimeFormatter, self).__init__(fmt)

    def formatTime(self, record, datefmt=None):
        return str(datetime.timedelta(seconds=record.created - self.start_time))

    def format(self, record):
        record.msg = str(record.msg).strip()
        return super(TimeFormatter, self).format(record)