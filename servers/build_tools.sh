#!/bin/bash
# Expected arguments:
# $1 = out_dir
# $2 = dist_dir
# $3 = build_number
# $4 = number of --parallel-thread (optional)

set -e

PROG_DIR=$(dirname "$0")
CURRENT_OS=$(uname | tr A-Z a-z)

echo "$*" > /dev/stderr

# TODO: Figure out why this doesn't work
# function die() {
#   echo "Usage: $0 <out_dir> <dest_dir> <build_number> [num_threads=47]" > /dev/stderr
#   exit 1
# }

# while [[ -n "$1" ]]; do
#   if [[ -z "$OUT_DIR" ]]; then
#     OUT_DIR="$1"
#   elif [[ -z "$DIST_DIR" ]]; then
#     DIST_DIR="$1"
#   elif [[ -z "$BNUM" ]]; then
#     BNUM="$1"
#   elif [[ -z "$NUM_THREADS" ]]; then
#     NUM_THREADS="$1"
#   else
#     die "[$0] Unknown parameter: $1"
#   fi
#   shift
# done
# 
# if [[ -z "$OUT_DIR"  ]]; then die "## Error: Missing out folder"; fi
# if [[ -z "$DIST_DIR" ]]; then die "## Error: Missing destination folder"; fi
# if [[ -z "$BNUM"     ]]; then die "## Error: Missing build number"; fi
# 
# if [[ "$OUT_DIR" != /* ]]
# then
#     pushd "$PROG_DIR"/../../..
#     OUT_DIR="$PWD/$OUT_DIR"
#     popd
# fi

# Of course we are running on build bots that have old.. old.. stuff on them.
# So let's make sure we have enums available.
# if [[ $CURRENT_OS == "linux" ]]; then
  # our gce instances pip is broken b/129467161
  # pip install enum34 --user
# fi

python $PROG_DIR/build_tools.py --out_dir $1 --dist_dir $2 --build-id $3
