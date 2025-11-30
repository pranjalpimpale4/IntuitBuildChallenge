package com.pranjal.assign2;

/**
 * ANSI Color Codes Utility for Terminal Output.
 * <p>Provides professional visual styling for console-based dashboards.
 * Uses ANSI escape sequences to enable color formatting in terminal environments
 * that support ANSI codes (most modern terminals, including Windows Terminal,
 * PowerShell, and Unix-based systems).</p>
 * 
 * <p>Color Scheme:
 * <ul>
 *   <li>CYAN_BOLD: Primary headers and platform branding</li>
 *   <li>RED_BOLD: Critical alerts and risk indicators</li>
 *   <li>GREEN_BOLD: Opportunities and positive recommendations</li>
 *   <li>YELLOW: Metrics and statistical data</li>
 *   <li>RESET: Returns terminal to default formatting</li>
 * </ul>
 * </p>
 * 
 * @author Pranjal
 * @version 1.3
 */
public class ConsoleColors {
    /**
     * ANSI escape sequence to reset all formatting to terminal defaults.
     * Should be used after any color code to prevent color bleeding.
     */
    public static final String RESET = "\033[0m";
    
    /**
     * ANSI escape sequence for bold red text.
     * Used for critical alerts, errors, and high-priority warnings.
     */
    public static final String RED_BOLD = "\033[1;31m";
    
    /**
     * ANSI escape sequence for bold green text.
     * Used for positive indicators, opportunities, and success messages.
     */
    public static final String GREEN_BOLD = "\033[1;32m";
    
    /**
     * ANSI escape sequence for bold cyan text.
     * Used for headers, platform branding, and primary section titles.
     */
    public static final String CYAN_BOLD = "\033[1;36m";
    
    /**
     * ANSI escape sequence for yellow text (non-bold).
     * Used for metrics, statistics, and informational data displays.
     */
    public static final String YELLOW = "\033[0;33m";
}

