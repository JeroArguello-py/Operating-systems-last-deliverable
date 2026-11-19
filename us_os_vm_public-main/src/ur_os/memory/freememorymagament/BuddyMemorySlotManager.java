package ur_os.memory.freememorymagament;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import ur_os.memory.ProcessMemoryManager;
import ur_os.memory.contiguous.PMM_Contiguous;
import ur_os.memory.segmentation.PMM_Segmentation;
import ur_os.memory.segmentation.SegmentTableEntry;
import ur_os.process.Process;

/**
 * Buddy System: memory is managed in blocks of size 2^k.
 * A free block is split into two "buddies" of size 2^(k-1);
 * when memory is released, a block is merged only with its buddy.
 */
public class BuddyMemorySlotManager extends FreeMemorySlotManager {

    private final int maxOrder;            // the total block size is 2^maxOrder
    private final TreeSet<Integer>[] free; // free[k] = base addresses of free blocks of size 2^k

    @SuppressWarnings("unchecked")
    public BuddyMemorySlotManager(int memSize) {
        super(memSize);
        this.list.clear(); // this manager does not use the parent class list

        int total = Integer.highestOneBit(memSize); // largest power of 2 <= memSize
        if (total != memSize) {
            System.out.println("Warning: Buddy uses " + total + " out of " + memSize + " units.");
        }
        this.maxOrder = Integer.numberOfTrailingZeros(total);
        this.free = new TreeSet[maxOrder + 1];
        for (int k = 0; k <= maxOrder; k++) {
            free[k] = new TreeSet<>();
        }
        free[maxOrder].add(0);
    }

    // Smallest k such that 2^k >= size
    private static int orderFor(int size) {
        return (size <= 1) ? 0 : 32 - Integer.numberOfLeadingZeros(size - 1);
    }

    @Override
    public MemorySlot getSlot(int size) {
        if (size <= 0) return null;
        int k = orderFor(size);
        if (k > maxOrder) return null;

        // Find the smallest available free block that can fit the requested size
        int j = k;
        while (j <= maxOrder && free[j].isEmpty()) {
            j++;
        }
        if (j > maxOrder) return null;

        int base = free[j].pollFirst(); // the block with the lowest address

        // Split the block until the required size is reached; the upper half remains free
        while (j > k) {
            j--;
            free[j].add(base + (1 << j));
        }
        // Return the entire block (2^k), making internal fragmentation visible
        return new MemorySlot(base, 1 << k);
    }

    private void freeBlock(int base, int size) {
        int k = orderFor(size); // recalculate the order; it is idempotent for 2^k -> k
        while (k < maxOrder) {
            int buddy = base ^ (1 << k);
            if (free[k].remove(buddy)) { // buddy is free: merge the blocks
                base = Math.min(base, buddy);
                k++;
            } else {
                break;
            }
        }
        free[k].add(base);
    }

    @Override
    public void reclaimMemory(Process p) {
        ProcessMemoryManager pmm = p.getPMM();
        switch (pmm.getType()) {
            case SEGMENTATION:
                PMM_Segmentation pmms = (PMM_Segmentation) pmm;
                ArrayList<SegmentTableEntry> segList = pmms.getSt().getTable();
                for (SegmentTableEntry ste : segList) {
                    MemorySlot s = ste.getMemorySlot();
                    if (s != null && s.getSize() > 0) {
                        freeBlock(s.getBase(), s.getSize());
                    }
                    ste.setValid(false);
                }
                break;
            default:
            case CONTIGUOUS:
                PMM_Contiguous pmmc = (PMM_Contiguous) pmm;
                MemorySlot s = pmmc.getMemorySlot();
                if (s != null && s.getSize() > 0) {
                    freeBlock(s.getBase(), s.getSize());
                }
                pmmc.setValid(false);
                break;
        }
    }

    // Number of free blocks (without merging across levels)
    @Override
    public int getSize() {
        int n = 0;
        for (TreeSet<Integer> t : free) n += t.size();
        return n;
    }

    @Override
    public String toString() {
        TreeMap<Integer, Integer> sorted = new TreeMap<>(); // base address -> block size
        for (int k = 0; k <= maxOrder; k++) {
            for (int base : free[k]) sorted.put(base, 1 << k);
        }
        StringBuilder sb = new StringBuilder();
        for (var e : sorted.entrySet()) {
            sb.append(new MemorySlot(e.getKey(), e.getValue()).toString()).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        BuddyMemorySlotManager m = new BuddyMemorySlotManager(1024);

        MemorySlot a = m.getSlot(100); // block [0,128]
        MemorySlot b = m.getSlot(200); // block [256,256]
        MemorySlot c = m.getSlot(60);  // block [128,64]
        System.out.println(m); // expected: [192,64], [512,512]

        m.freeBlock(a.getBase(), a.getSize());
        m.freeBlock(c.getBase(), c.getSize()); // c merges with its buddy and then with [0,128]
        System.out.println(m); // expected: [0,256], [512,512]

        m.freeBlock(b.getBase(), b.getSize());
        System.out.println(m); // expected: [0,1024] (full memory)
    }
}