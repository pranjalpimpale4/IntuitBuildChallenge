package com.pranjal.assign1;

/**
 * Type-safe queue command signals for system control.
 * 
 * Using enum instead of generic Object provides type safety,
 * explicit intent, and singleton guarantee.
 * 
 * @author Pranjal
 * @version 2.1
 */
public enum QueueCommand {
    
    /**
     * Poison Pill - signals consumers to shut down gracefully.
     * 
     * When received, consumer should:
     * 1. Stop processing
     * 2. Relay the pill (put it back for other consumers)
     * 3. Exit
     */
    POISON_PILL
}
