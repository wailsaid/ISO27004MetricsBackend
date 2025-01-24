# Variables
MAVEN_OPTS ?= -Xmx512m -Xms256m
MAVEN_CMD = mvn
MAVEN_WRAPPER = ./mvnw
USE_WRAPPER ?= true

# Use Maven Wrapper if available
ifeq ($(USE_WRAPPER), true)
    ifeq (, $(shell which ./mvnw))
        $(warning Maven Wrapper not found. Using system Maven.)
        MAVEN_CMD = mvn
    else
        MAVEN_CMD = $(MAVEN_WRAPPER)
    endif
endif

# Targets
.PHONY: all clean build test package install offline skip-tests parallel cleanup

# Default target
all: build

# Clean the project
clean:
	$(MAVEN_CMD) clean

# Build the project (skip tests and Javadoc)
build:
	$(MAVEN_CMD) clean install -DskipTests -Dmaven.javadoc.skip=true -Dmaven.source.skip=true

# Run tests
test:
	$(MAVEN_CMD) test

# Package the project (skip tests)
package:
	$(MAVEN_CMD) package -DskipTests

# Install the project (skip tests)
install:
	$(MAVEN_CMD) install -DskipTests

# Build in offline mode
offline:
	$(MAVEN_CMD) -o clean install

# Skip tests explicitly
skip-tests:
	$(MAVEN_CMD) clean install -DskipTests

# Build with parallel threads (adjust the number of threads as needed)
parallel:
	$(MAVEN_CMD) clean install -T 2

# Clean up local Maven repository
cleanup:
	@echo "Cleaning up local Maven repository..."
	@rm -rf ~/.m2/repository

# Profile for low-end machines
low-end:
	$(MAVEN_CMD) clean install -P low-end

# Help target
help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  all         : Build the project (default)"
	@echo "  clean       : Clean the project"
	@echo "  build       : Build the project (skip tests and Javadoc)"
	@echo "  test        : Run tests"
	@echo "  package     : Package the project (skip tests)"
	@echo "  install     : Install the project (skip tests)"
	@echo "  offline     : Build in offline mode"
	@echo "  skip-tests  : Build and skip tests"
	@echo "  parallel    : Build with parallel threads"
	@echo "  cleanup     : Clean up local Maven repository"
	@echo "  low-end     : Build using low-end profile"
	@echo "  help        : Show this help message"
