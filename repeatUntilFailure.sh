#!/bin/bash

# Ensure that a command is provided as an argument
if [ $# -eq 0 ]; then
    echo "No command provided. Usage: ./run_until_fail.sh <command>"
    exit 1
fi

# Command to run (passed as script arguments)
COMMAND="$@"
MAX_RETRIES=10  # Set the maximum number of retries
COUNTER=0  # Initialize the retry counter
CHECK_INTERVAL=5  # Interval in seconds to check if the command is still running
ALERT_INTERVAL=120  # Interval in seconds to run jcmd if the command is still running

while [ $COUNTER -lt $MAX_RETRIES ]; do
    # Increment the retry counter
    COUNTER=$((COUNTER + 1))
    echo "Attempt $COUNTER of $MAX_RETRIES..."

    # Start the command in the background
    bash -c "$COMMAND" &
    COMMAND_PID=$!

    # Track elapsed time
    ELAPSED_TIME=0

    # Check periodically if the command has completed
    while ps -p $COMMAND_PID > /dev/null; do
        sleep $CHECK_INTERVAL
        ELAPSED_TIME=$((ELAPSED_TIME + CHECK_INTERVAL))

        # Run jcmd every ALERT_INTERVAL (60 seconds)
        if [ $((ELAPSED_TIME % ALERT_INTERVAL)) -eq 0 ]; then
            echo "Warning: The command has been running for more than $ELAPSED_TIME seconds."
            echo "\n\n----------------------------------------------------------------------------------------\n\n"

            # Run the jcmd command to print thread information
            jcmd $(jps -v | grep SpockTestConfig.groovy | awk '{print $1}') Thread.print
            echo "\n\n----------------------------------------------------------------------------------------\n\n"
        fi
    done

    # Capture the exit status
    wait $COMMAND_PID
    STATUS=$?

    # Check if the command failed
    if [ $STATUS -ne 0 ]; then
        echo "The command failed with exit status $STATUS. Exiting loop."
        break
    fi

    # Check if the retry limit has been reached
    if [ $COUNTER -ge $MAX_RETRIES ]; then
        echo "Reached the maximum number of retries ($MAX_RETRIES). Exiting."
        break
    fi
done
