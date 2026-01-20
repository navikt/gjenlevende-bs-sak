#!/bin/bash

# Shared utilities for startup scripts
# Colors and spinner animations

# Farger og stiler
BOLD='\033[1m'
DIM='\033[2m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Braille spinner array
SPINNER=("⠋" "⠙" "⠹" "⠸" "⠼" "⠴" "⠦" "⠧" "⠇" "⠏")

cleanup() {
    tput cnorm 2>/dev/null
}
trap cleanup EXIT

print_header() {
    local title=$1
    echo ""
    echo -e "${BOLD}╭───────────────────────────────────────────────────────────────╮${NC}"
    printf "${BOLD}│${NC}  ${CYAN}⚡${NC} ${BOLD}Gjenlevende BS Sak${NC} - %-36s${BOLD}│${NC}\n" "$title"
    echo -e "${BOLD}╰───────────────────────────────────────────────────────────────╯${NC}"
    echo ""
}

print_success_box() {
    local msg=$1
    echo ""
    echo -e "${BOLD}╭───────────────────────────────────────────────────────────────╮${NC}"
    printf "${BOLD}│${NC}  ${GREEN}✓${NC} %-58s${BOLD}│${NC}\n" "$msg"
    echo -e "${BOLD}╰───────────────────────────────────────────────────────────────╯${NC}"
    echo ""
}

spin() {
    local pid=$1
    local msg=$2
    local i=0
    tput civis
    while kill -0 $pid 2>/dev/null; do
        printf "\r  ${CYAN}${SPINNER[$i]}${NC} ${msg}"
        i=$(( (i + 1) % 10 ))
        sleep 0.1
    done
    printf "\r\033[K"
    tput cnorm
}

wait_for_condition() {
    local condition_cmd=$1
    local msg=$2
    local timeout=${3:-60}
    local i=0
    local elapsed=0
    
    tput civis
    while ! eval "$condition_cmd" 2>/dev/null; do
        printf "\r  ${CYAN}${SPINNER[$i]}${NC} ${msg}"
        i=$(( (i + 1) % 10 ))
        sleep 0.1
        elapsed=$(echo "$elapsed + 0.1" | bc)
        if (( $(echo "$elapsed >= $timeout" | bc -l) )); then
            printf "\r\033[K"
            tput cnorm
            return 1
        fi
    done
    printf "\r\033[K"
    tput cnorm
    return 0
}

print_step() {
    local msg=$1
    echo -e "${BLUE}░▒▓${NC} ${BOLD}${msg}${NC}"
}

print_ok() {
    local msg=$1
    echo -e "  ${GREEN}✓${NC} ${msg}"
}

print_fail() {
    local msg=$1
    echo -e "  ${RED}✗${NC} ${msg}"
}

print_warn() {
    local msg=$1
    echo -e "  ${YELLOW}!${NC} ${msg}"
}

is_container_running() {
    local container=$1
    docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${container}$"
}

is_port_responding() {
    local url=$1
    curl -s "$url" > /dev/null 2>&1
}
