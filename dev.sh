#!/bin/bash

# Development script for Clojure Todo App
# Usage:
#   ./dev.sh              # Run both backend and frontend with file watching (default)
#   ./dev.sh backend      # Run backend only
#   ./dev.sh frontend     # Run frontend only
#   ./dev.sh both         # Run both backend and frontend (no file watching)
#   ./dev.sh watch        # Run both backend and frontend with file watching

BACKEND_PORT=${PORT:-3000}
FRONTEND_PORT=5173

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

run_backend() {
    echo -e "${BLUE}Starting Clojure backend on port ${BACKEND_PORT}...${NC}"
    clj -M -m clojure-todo.core
}

run_frontend() {
    if [ ! -d "frontend" ]; then
        echo -e "${YELLOW}Error: frontend directory not found${NC}"
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        echo -e "${YELLOW}Error: npm not found. Please install Node.js and npm${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}Starting frontend on port ${FRONTEND_PORT}...${NC}"
    cd frontend
    npm run dev
}

run_backend_watch() {
    if ! command -v entr &> /dev/null; then
        echo -e "${YELLOW}Warning: 'entr' not found. Install it for file watching:${NC}"
        echo "  - Arch/Manjaro: sudo pacman -S entr"
        echo "  - Ubuntu/Debian: sudo apt install entr"
        echo "  - macOS: brew install entr"
        echo ""
        echo "Running backend without file watching..."
        run_backend
        return
    fi
    
    echo -e "${BLUE}Starting Clojure backend with file watching on port ${BACKEND_PORT}...${NC}"
    echo -e "${YELLOW}Watching for changes in src/ directory...${NC}"
    echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
    echo ""
    
    # Watch for changes and restart the server
    find src -name "*.clj" | entr -r -s "clj -M -m clojure-todo.core" &
    BACKEND_PID=$!
    
    # Wait for Ctrl+C
    trap "kill $BACKEND_PID 2>/dev/null; exit" INT TERM
    wait $BACKEND_PID
}

run_watch() {
    # Check prerequisites
    if [ ! -d "frontend" ]; then
        echo -e "${YELLOW}Error: frontend directory not found${NC}"
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        echo -e "${YELLOW}Error: npm not found. Please install Node.js and npm${NC}"
        exit 1
    fi
    
    if ! command -v clj &> /dev/null; then
        echo -e "${YELLOW}Error: clj (Clojure CLI) not found. Please install Clojure CLI${NC}"
        exit 1
    fi
    
    if ! command -v entr &> /dev/null; then
        echo -e "${YELLOW}Warning: 'entr' not found. Install it for backend file watching:${NC}"
        echo "  - Arch/Manjaro: sudo pacman -S entr"
        echo "  - Ubuntu/Debian: sudo apt install entr"
        echo "  - macOS: brew install entr"
        echo ""
        echo "Running without backend file watching..."
        USE_ENTR=false
    else
        USE_ENTR=true
    fi
    
    echo -e "${GREEN}Starting both backend and frontend with file watching...${NC}"
    echo ""
    
    # Start backend with file watching
    if [ "$USE_ENTR" = true ]; then
        echo -e "${BLUE}Starting Clojure backend with file watching on port ${BACKEND_PORT}...${NC}"
        echo -e "${YELLOW}Watching for changes in src/ directory...${NC}"
        find src -name "*.clj" | entr -r -s "clj -M -m clojure-todo.core" &
        BACKEND_PID=$!
    else
        echo -e "${BLUE}Starting Clojure backend on port ${BACKEND_PORT}...${NC}"
        clj -M -m clojure-todo.core &
        BACKEND_PID=$!
    fi
    
    # Wait a bit for backend to start
    sleep 3
    
    # Check if backend is still running
    if ! kill -0 $BACKEND_PID 2>/dev/null; then
        echo -e "${YELLOW}Error: Backend failed to start${NC}"
        exit 1
    fi
    
    # Start frontend (Vite has built-in file watching)
    echo -e "${BLUE}Starting frontend with file watching on port ${FRONTEND_PORT}...${NC}"
    echo -e "${YELLOW}Frontend uses Vite's built-in HMR (Hot Module Replacement)${NC}"
    (cd frontend && npm run dev) &
    FRONTEND_PID=$!
    
    # Wait a moment for frontend to start
    sleep 2
    
    # Check if frontend is still running
    if ! kill -0 $FRONTEND_PID 2>/dev/null; then
        echo -e "${YELLOW}Error: Frontend failed to start${NC}"
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    
    echo ""
    echo -e "${GREEN}✓ Backend running on http://localhost:${BACKEND_PORT} (PID: $BACKEND_PID)${NC}"
    if [ "$USE_ENTR" = true ]; then
        echo -e "${GREEN}  - Watching for changes in src/ directory${NC}"
    fi
    echo -e "${GREEN}✓ Frontend running on http://localhost:${FRONTEND_PORT} (PID: $FRONTEND_PID)${NC}"
    echo -e "${GREEN}  - Vite HMR enabled (automatic reload on file changes)${NC}"
    echo -e "${YELLOW}Press Ctrl+C to stop both servers${NC}"
    echo ""
    
    # Function to cleanup on exit
    cleanup() {
        echo ""
        echo -e "${YELLOW}Stopping servers...${NC}"
        kill $BACKEND_PID 2>/dev/null || true
        kill $FRONTEND_PID 2>/dev/null || true
        # Kill any child processes
        pkill -P $BACKEND_PID 2>/dev/null || true
        pkill -P $FRONTEND_PID 2>/dev/null || true
        exit
    }
    
    # Wait for Ctrl+C
    trap cleanup INT TERM
    
    # Wait for both processes (or until interrupted)
    wait $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
}

run_both() {
    # Check prerequisites
    if [ ! -d "frontend" ]; then
        echo -e "${YELLOW}Error: frontend directory not found${NC}"
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        echo -e "${YELLOW}Error: npm not found. Please install Node.js and npm${NC}"
        exit 1
    fi
    
    if ! command -v clj &> /dev/null; then
        echo -e "${YELLOW}Error: clj (Clojure CLI) not found. Please install Clojure CLI${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Starting both backend and frontend...${NC}"
    echo ""
    
    # Start backend in background
    echo -e "${BLUE}Starting Clojure backend on port ${BACKEND_PORT}...${NC}"
    clj -M -m clojure-todo.core &
    BACKEND_PID=$!
    
    # Wait a bit for backend to start
    sleep 3
    
    # Check if backend is still running
    if ! kill -0 $BACKEND_PID 2>/dev/null; then
        echo -e "${YELLOW}Error: Backend failed to start${NC}"
        exit 1
    fi
    
    # Start frontend in background
    echo -e "${BLUE}Starting frontend on port ${FRONTEND_PORT}...${NC}"
    (cd frontend && npm run dev) &
    FRONTEND_PID=$!
    
    # Wait a moment for frontend to start
    sleep 2
    
    # Check if frontend is still running
    if ! kill -0 $FRONTEND_PID 2>/dev/null; then
        echo -e "${YELLOW}Error: Frontend failed to start${NC}"
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    
    echo ""
    echo -e "${GREEN}✓ Backend running on http://localhost:${BACKEND_PORT} (PID: $BACKEND_PID)${NC}"
    echo -e "${GREEN}✓ Frontend running on http://localhost:${FRONTEND_PORT} (PID: $FRONTEND_PID)${NC}"
    echo -e "${YELLOW}Press Ctrl+C to stop both servers${NC}"
    echo ""
    
    # Function to cleanup on exit
    cleanup() {
        echo ""
        echo -e "${YELLOW}Stopping servers...${NC}"
        kill $BACKEND_PID 2>/dev/null || true
        kill $FRONTEND_PID 2>/dev/null || true
        # Kill any child processes
        pkill -P $BACKEND_PID 2>/dev/null || true
        pkill -P $FRONTEND_PID 2>/dev/null || true
        exit
    }
    
    # Wait for Ctrl+C
    trap cleanup INT TERM
    
    # Wait for both processes (or until interrupted)
    wait $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
}

# Main script logic
case "${1:-watch}" in
    backend)
        run_backend
        ;;
    frontend)
        run_frontend
        ;;
    both)
        run_both
        ;;
    watch)
        run_watch
        ;;
    *)
        echo "Usage: $0 [backend|frontend|both|watch]"
        echo ""
        echo "Options:"
        echo "  (no args) - Run both backend and frontend with file watching (default)"
        echo "  backend   - Run backend only"
        echo "  frontend  - Run frontend only"
        echo "  both      - Run both backend and frontend"
        echo "  watch     - Run both backend and frontend with file watching"
        exit 1
        ;;
esac

