package ur_os.memory.freememorymagament;

/**
 * Free memory slot manager using the Worst Fit strategy.
 *
 * @author super
 */
public class WorstFitMemorySlotManager extends FreeMemorySlotManager {

    public WorstFitMemorySlotManager(int memSize) {
        super(memSize);
    }

    @Override
    public MemorySlot getSlot(int size) {

        // Step 1: find the largest free slot that can fit the requested size.
        // Only a strictly larger slot replaces the current candidate,
        // so the first one wins in case of a tie.
        MemorySlot largest = null;

        for (MemorySlot candidate : list) {
            boolean fits = candidate.canContain(size);
            boolean isBigger = (largest == null) || (candidate.getSize() > largest.getSize());

            if (fits && isBigger) {
                largest = candidate;
            }
        }

        // Step 2: no suitable slot was found, so an error is reported and null is returned.
        if (largest == null) {
            System.out.println("Error - Memory cannot allocate a slot big enough for the requested memory");
            return null;
        }

        // Step 3a: the slot exactly matches the requested size,
        // so it is allocated entirely and removed from the free list.
        if (largest.getSize() == size) {
            list.remove(largest);
            return largest;
        }

        // Step 3b: the slot is larger than requested, so it is split:
        // the requested portion is allocated and the original free slot
        // is left with an updated base address and a smaller size.
        return largest.assignMemory(size);
    }

}