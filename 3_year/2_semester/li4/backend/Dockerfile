# Use the same base image as specified in docker-compose.yml for consistency
FROM gradle:8.6-jdk21

# Install fontconfig and a common font package (e.g., ttf-dejavu for sans-serif)
# This assumes the base image is Debian-based. If it were Alpine, it would be 'apk add fontconfig ttf-dejavu'
# For Debian/Ubuntu-based:


# Set the working directory
WORKDIR /workspace

# Copy the project files
COPY . /workspace

# Set default command (if any) or entrypoint.
# For now, we keep it simple, as docker-compose will override the command.
# ENTRYPOINT ["gradle"]
# CMD ["sleep", "infinity"]
