#!/bin/bash

# Farger og stiler
BOLD='\033[1m'
DIM='\033[2m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Spinner
SPINNER=("⠋" "⠙" "⠹" "⠸" "⠼" "⠴" "⠦" "⠧" "⠇" "⠏")

cleanup() {
    tput cnorm 2>/dev/null
}
trap cleanup EXIT

print_header() {
    echo ""
    echo -e "${BOLD}╭───────────────────────────────────────────────────────────────╮${NC}"
    echo -e "${BOLD}│${NC}  ${CYAN}⚡${NC} ${BOLD}Gjenlevende BS Sak${NC} - Test Suite                            ${BOLD}│${NC}"
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

main() {
    local start=$(date +%s)
    print_header
    
    # Kompilering
    echo -e "${BLUE}░▒▓${NC} ${BOLD}Kompilerer...${NC}"
    
    mvn compile test-compile -q -DskipTests > /dev/null 2>&1 &
    spin $! "Kompilerer kode..."
    wait $!
    
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}✓${NC} Kompilering fullført"
    else
        echo -e "  ${RED}✗${NC} Kompilering feilet"
        exit 1
    fi
    
    echo ""
    echo -e "${BLUE}░▒▓${NC} ${BOLD}Kjører tester...${NC}"
    echo ""
    
    # Temp fil for output
    local temp=$(mktemp)
    local current_test=""
    local test_count=0
    local has_failures=0
    
    # Start tester i bakgrunnen og lagre output
    mvn surefire:test 2>&1 > "$temp" &
    local mvn_pid=$!
    
    tput civis  # Skjul cursor
    
    # Følg med på output og vis fremdrift
    local spin_idx=0
    while kill -0 $mvn_pid 2>/dev/null; do
        # Les siste "Running" linje
        local new_test=$(grep -o "Running [^ ]*" "$temp" 2>/dev/null | tail -1 | sed 's/Running //' | sed 's/no\.nav\.gjenlevende\.bs\.sak\.//')
        
        if [ -n "$new_test" ] && [ "$new_test" != "$current_test" ]; then
            # Ny test startet - skriv ut forrige som ferdig hvis den finnes
            if [ -n "$current_test" ]; then
                # Finn resultat for forrige test
                local result=$(grep "Tests run:.*-- in.*${current_test}" "$temp" 2>/dev/null | tail -1)
                if [ -n "$result" ]; then
                    local count=$(echo "$result" | grep -oE "Tests run: [0-9]+" | grep -oE "[0-9]+")
                    local time=$(echo "$result" | grep -oE "[0-9.]+ s" | head -1)
                    if [[ "$result" =~ "Failures: 0" && "$result" =~ "Errors: 0" ]]; then
                        printf "\r\033[K  ${GREEN}✓${NC} ${current_test} ${DIM}(${count} tests, ${time})${NC}\n"
                    else
                        printf "\r\033[K  ${RED}✗${NC} ${current_test} ${DIM}(${count} tests, ${time})${NC}\n"
                        has_failures=1
                    fi
                fi
            fi
            current_test="$new_test"
            test_count=$((test_count + 1))
        fi
        
        # Vis spinner for nåværende test
        if [ -n "$current_test" ]; then
            printf "\r  ${CYAN}${SPINNER[$spin_idx]}${NC} ${DIM}[${test_count}]${NC} ${current_test}"
        fi
        
        spin_idx=$(( (spin_idx + 1) % 10 ))
        sleep 0.1
    done
    
    # Vent på maven og få exit code
    wait $mvn_pid
    local exit_code=$?
    
    # Skriv ut siste test
    if [ -n "$current_test" ]; then
        local result=$(grep "Tests run:.*-- in.*${current_test}" "$temp" 2>/dev/null | tail -1)
        if [ -n "$result" ]; then
            local count=$(echo "$result" | grep -oE "Tests run: [0-9]+" | grep -oE "[0-9]+")
            local time=$(echo "$result" | grep -oE "[0-9.]+ s" | head -1)
            if [[ "$result" =~ "Failures: 0" && "$result" =~ "Errors: 0" ]]; then
                printf "\r\033[K  ${GREEN}✓${NC} ${current_test} ${DIM}(${count} tests, ${time})${NC}\n"
            else
                printf "\r\033[K  ${RED}✗${NC} ${current_test} ${DIM}(${count} tests, ${time})${NC}\n"
                has_failures=1
            fi
        fi
    fi
    
    tput cnorm  # Vis cursor
    
    # Parse resultater fra Results seksjonen i temp filen
    local results_line=$(grep "Tests run:" "$temp" | grep -v "in " | tail -1)
    local total=$(echo "$results_line" | grep -oE "Tests run: [0-9]+" | grep -oE "[0-9]+")
    local failed=$(echo "$results_line" | grep -oE "Failures: [0-9]+" | grep -oE "[0-9]+")
    local errors=$(echo "$results_line" | grep -oE "Errors: [0-9]+" | grep -oE "[0-9]+")
    local skipped=$(echo "$results_line" | grep -oE "Skipped: [0-9]+" | grep -oE "[0-9]+")
    
    # Defaults
    total=${total:-0}
    failed=${failed:-0}
    errors=${errors:-0}
    skipped=${skipped:-0}
    
    local passed=$((total - failed - errors - skipped))
    local end=$(date +%s)
    local duration=$((end - start))
    
    rm -f "$temp"
    
    # Summary box
    echo ""
    echo -e "${BOLD}╭───────────────────────────────────────────────────────────────╮${NC}"
    if [ "$failed" -eq 0 ] && [ "$errors" -eq 0 ]; then
        echo -e "${BOLD}│${NC}  ${GREEN}✓${NC} ${BOLD}Alle tester bestått!${NC}                                       ${BOLD}│${NC}"
    else
        echo -e "${BOLD}│${NC}  ${RED}✗${NC} ${BOLD}Noen tester feilet${NC}                                         ${BOLD}│${NC}"
    fi
    echo -e "${BOLD}├───────────────────────────────────────────────────────────────┤${NC}"
    printf "${BOLD}│${NC}  Totalt:   %-51s${BOLD}│${NC}\n" "${total} tester"
    printf "${BOLD}│${NC}  ${GREEN}Bestått:${NC}  %-51s${BOLD}│${NC}\n" "${passed}"
    [ "$failed" -gt 0 ] && printf "${BOLD}│${NC}  ${RED}Feilet:${NC}   %-51s${BOLD}│${NC}\n" "${failed}"
    [ "$skipped" -gt 0 ] && printf "${BOLD}│${NC}  ${YELLOW}Hoppet:${NC}   %-51s${BOLD}│${NC}\n" "${skipped}"
    printf "${BOLD}│${NC}  Tid:      %-51s${BOLD}│${NC}\n" "${duration}s"
    echo -e "${BOLD}╰───────────────────────────────────────────────────────────────╯${NC}"
    echo ""
    
    [ "$failed" -gt 0 ] || [ "$errors" -gt 0 ] && exit 1
    exit 0
}

main "$@"
