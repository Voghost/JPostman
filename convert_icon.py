#!/usr/bin/env python3
"""
Simple SVG to PNG converter for JPostman icon
Creates multiple sizes for different display contexts
"""
import os
import subprocess
import sys

# Icon sizes needed for Java application
SIZES = [16, 32, 48, 64, 128, 256, 512]

svg_path = "src/main/resources/icons/jpostman.svg"
output_dir = "src/main/resources/icons"

# Check if we have the svg file
if not os.path.exists(svg_path):
    print(f"Error: {svg_path} not found")
    sys.exit(1)

print("Converting SVG to PNG...")

# Try using sips (macOS built-in tool)
try:
    for size in SIZES:
        output_path = f"{output_dir}/jpostman_{size}.png"

        # Use sips to convert
        cmd = [
            "sips",
            "-s", "format", "png",
            "-z", str(size), str(size),
            svg_path,
            "--out", output_path
        ]

        result = subprocess.run(cmd, capture_output=True, text=True)

        if result.returncode == 0:
            print(f"✓ Created {size}x{size} icon")
        else:
            print(f"✗ Failed to create {size}x{size} icon")
            print(result.stderr)

except Exception as e:
    print(f"Error: {e}")
    print("\nAlternatively, you can:")
    print("1. Install librsvg: brew install librsvg")
    print("2. Or use online tool: https://cloudconvert.com/svg-to-png")
    sys.exit(1)

print("\nDone! Icons created in", output_dir)
