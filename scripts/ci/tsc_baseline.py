#!/usr/bin/env python3
"""Ratchet for TypeScript errors: fail on errors that are not in the committed baseline.

Reads `tsc --noEmit --pretty false` output on stdin and compares the number of errors per
(file, TS code) with the baseline JSON file. Line numbers are ignored so that edits elsewhere
in a file do not break the check. Fails when a count grows (a new error) and when it shrinks
(an error was fixed: lower the baseline in the same change, so it never grows back).

Errors without a location (`error TS2688: ...`) count under "<global>". With
--tsc-status, a failing tsc whose output holds no parsable error also fails the check.

Usage: tsc --noEmit --pretty false | tsc_baseline.py BASELINE.json [--tsc-status N] [--update]
"""

import json
import re
import sys
from collections import Counter

ERROR = re.compile(r"^(?P<file>.+?)\(\d+,\d+\): error (?P<code>TS\d+):")
GLOBAL_ERROR = re.compile(r"^error (?P<code>TS\d+):")


def error_key(line: str) -> str | None:
    if m := ERROR.match(line):
        return f"{m['file']} {m['code']}"
    if m := GLOBAL_ERROR.match(line):
        return f"<global> {m['code']}"
    return None


def main() -> int:
    if len(sys.argv) < 2:
        print(__doc__, file=sys.stderr)
        return 2
    path = sys.argv[1]
    args = sys.argv[2:]
    tsc_status = int(args[args.index("--tsc-status") + 1]) if "--tsc-status" in args else None
    output = sys.stdin.read()
    current = Counter(key for key in map(error_key, output.splitlines()) if key)

    if tsc_status not in (None, 0) and not current:
        print(output, file=sys.stderr)
        print(f"tsc failed (exit {tsc_status}) without reporting an error.", file=sys.stderr)
        return 1

    if "--update" in args:
        with open(path, "w", encoding="utf-8") as f:
            json.dump(dict(sorted(current.items())), f, indent=2)
            f.write("\n")
        print(f"Wrote {sum(current.values())} baseline errors to {path}")
        return 0

    with open(path, encoding="utf-8") as f:
        baseline = Counter(json.load(f))

    new = current - baseline
    fixed = baseline - current
    if new:
        print(output, file=sys.stderr)
        print("New TypeScript errors (not in the baseline):", file=sys.stderr)
        for key, count in sorted(new.items()):
            print(f"  {key} (+{count})", file=sys.stderr)
    if fixed:
        print(f"Fixed baseline errors; lower the counts in {path}:", file=sys.stderr)
        for key, count in sorted(fixed.items()):
            print(f"  {key} (-{count})", file=sys.stderr)
    if new or fixed:
        return 1
    print(f"TypeScript: {sum(current.values())} errors, all in the baseline.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
