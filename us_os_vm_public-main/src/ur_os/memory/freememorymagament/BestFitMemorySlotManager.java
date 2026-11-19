package ur_os.memory.freememorymagament;

/**
 * Free memory slot manager using the Best Fit strategy.
 *
 * @author super
 */
public class BestFitMemorySlotManager extends FreeMemorySlotManager {

    public BestFitMemorySlotManager(int memSize) {
        super(memSize);
    }

    @Override
    public MemorySlot getSlot(int size) {

        // Step 1: find the smallest free slot that can fit the requested size.
        // The list is sorted by address, not by size, so the entire list must
        // be traversed. Only a strictly smaller slot replaces the current
        // candidate, so the first one wins in case of a tie.
        MemorySlot tightest = null;

        for (MemorySlot candidate : list) {
            boolean fits = candidate.canContain(size);
            boolean isSmaller = (tightest == null) || (candidate.getSize() < tightest.getSize());

            if (fits && isSmaller) {
                tightest = candidate;
            }
        }

        // Step 2: no suitable slot was found, so an error is reported and null is returned.
        if (tightest == null) {
            System.out.println("Error - Memory cannot allocate a slot big enough for the requested memory");
            return null;
        }

        // Step 3a: the slot exactly matches the requested size,
        // so it is allocated entirely and removed from the free list.
        if (tightest.getSize() == size) {
            list.remove(tightest);
            return tightest;
        }

        // Step 3b: the slot is larger than requested, so it is split:
        // the requested portion is allocated and the remaining space
        // stays in the list as a free slot.
        return tightest.assignMemory(size);
    }

}