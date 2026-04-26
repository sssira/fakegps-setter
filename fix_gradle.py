import os

path = 'app/build.gradle'
with open(path, 'r') as f:
    lines = f.readlines()

with open(path, 'w') as f:
    for line in lines:
        # Bypass baris yang bikin crash (pembacaan local.properties manual)
        if 'local.properties' in line or 'properties.load' in line:
            f.write(f"// {line}")
        else:
            f.write(line)
