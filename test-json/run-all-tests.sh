#
# Constants
#

# Script Constants
readonly VALID_PLATFORMS="android ios web"
readonly OPTIONS_PATH="./options"

# Terminal Formatting
export FONT_RESET=$(tput sgr0)
export FONT_BOLD=$(tput bold)

#
# Helper Functions
#

contains() {
    [[ "$1" =~ (^|[[:space:]])"$2"($|[[:space:]]) ]]
}

#
# Arguments
#

print_usage() {
    printf "${FONT_BOLD}Usage:${FONT_RESET} $0 --platform={android|ios|web} --jar=path/to/jar \n"
}

validate_args() {
    if ! contains "$VALID_PLATFORMS" "$platform"; then
        printf "${FONT_BOLD}Error:${FONT_RESET} Invalid platform\n"
        print_usage
        exit 1
    fi

    if [[ ! -f "$jar" ]]; then
        printf "${FONT_BOLD}Error:${FONT_RESET} Invalid jar path\n"
        print_usage
        exit 1
    fi
}

validate_paths() {
    if [[ ! -d "$OPTIONS_PATH" ]]; then
        printf "${FONT_BOLD}Error:${FONT_RESET} The ./options path does not exist, make sure to run this from within the test-json directory\n"
        print_usage
        exit 1
    fi
}

parse_args() {
    while [ $# -gt 0 ]; do
        case "$1" in
            --platform=*)
                platform="${1#*=}"
                ;;
            --jar=*)
                jar="${1#*=}"
                ;;
            *)
                print_usage
                exit 1
        esac
        shift
    done

    validate_args
    validate_paths
}

#
# Run Tests
#

reset() {
    rm -rf ./smartype smartype-dist
}

print_tests() {
    printf "${FONT_BOLD}Running Code Generation Tests${FONT_RESET}\n\n"
    printf "${FONT_BOLD}Platform:${FONT_RESET} $platform\n"    

    printf "${FONT_BOLD}Testing Options Files:${FONT_RESET}\n"
    for i in $OPTIONS_PATH/*.json; do
        printf "  ${i##*/}\n"
    done
    printf "\n\n"
}

run_tests() {
    for i in $OPTIONS_PATH/*.json; do
        printf "${FONT_BOLD}%s\n" "--------------------------------------------------------------------------------"
        printf "Testing: ${i##*/}\n"
        printf "%s${FONT_RESET}\n\n" "--------------------------------------------------------------------------------"

        # Generate config
        echo "{\"${platform}Options\": {\"enabled\": true}, \"binaryOutputDirectory\": \"smartype-dist\", \"apiSchemaFile\": \"$i\"}" > ./smartype.config.json

        # Run test
        reset
        java -jar $jar generate --config ./smartype.config.json
        
        # Check result
        if [[ $? -eq 1 ]]; then
            printf "\n${FONT_BOLD}Test FAILED!${FONT_RESET}\n"
            reset
            exit 1
        fi

        printf "\n"
    done
    reset
}

#
# Main
#

parse_args "$@"
print_tests
run_tests
