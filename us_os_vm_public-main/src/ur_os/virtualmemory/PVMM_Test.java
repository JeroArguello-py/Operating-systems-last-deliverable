package ur_os.virtualmemory;

import java.util.LinkedList;
import ur_os.memory.paging.PageTable;

public class PVMM_Test {

    public static void main(String[] args) {
        int passed = 0;
        int total = 0;

        System.out.println("====== RUNNING PVMM UNIT TESTS ======");

        // --- FIFO TESTS ---
        total++;
        if (testFIFO_Clocks()) {
            System.out.println("PASS: testFIFO_Clocks");
            passed++;
        } else {
            System.err.println("FAIL: testFIFO_Clocks");
        }

        total++;
        if (testFIFO_EqualClocks_MemoryAccessOrder()) {
            System.out.println("PASS: testFIFO_EqualClocks_MemoryAccessOrder");
            passed++;
        } else {
            System.err.println("FAIL: testFIFO_EqualClocks_MemoryAccessOrder");
        }

        // --- LRU TESTS ---
        total++;
        if (testLRU_Basic()) {
            System.out.println("PASS: testLRU_Basic");
            passed++;
        } else {
            System.err.println("FAIL: testLRU_Basic");
        }

        total++;
        if (testLRU_UnaccessedPage()) {
            System.out.println("PASS: testLRU_UnaccessedPage");
            passed++;
        } else {
            System.err.println("FAIL: testLRU_UnaccessedPage");
        }

        // --- LFU TESTS ---
        total++;
        if (testLFU_Basic()) {
            System.out.println("PASS: testLFU_Basic");
            passed++;
        } else {
            System.err.println("FAIL: testLFU_Basic");
        }

        total++;
        if (testLFU_TieBreakFIFO()) {
            System.out.println("PASS: testLFU_TieBreakFIFO");
            passed++;
        } else {
            System.err.println("FAIL: testLFU_TieBreakFIFO");
        }

        // --- MFU TESTS ---
        total++;
        if (testMFU_Basic()) {
            System.out.println("PASS: testMFU_Basic");
            passed++;
        } else {
            System.err.println("FAIL: testMFU_Basic");
        }

        total++;
        if (testMFU_TieBreakFIFO()) {
            System.out.println("PASS: testMFU_TieBreakFIFO");
            passed++;
        } else {
            System.err.println("FAIL: testMFU_TieBreakFIFO");
        }

        // --- COMPREHENSIVE DIFFERENTIATION TEST ---
        total++;
        if (testDifferentialScenario()) {
            System.out.println("PASS: testDifferentialScenario (FIFO vs LRU vs LFU vs MFU)");
            passed++;
        } else {
            System.err.println("FAIL: testDifferentialScenario");
        }

        System.out.println("=====================================");
        System.out.println("RESULTS: " + passed + "/" + total + " tests passed.");

        if (passed != total) {
            System.exit(1);
        }
    }

    private static PageTable createSamplePageTable(int numPages, int[] validPages, int[] clocks) {
        PageTable pt = new PageTable(numPages * 64, validPages.length, true);
        for (int i = 0; i < numPages; i++) {
            pt.addFrameID(i, false);
        }
        for (int i = 0; i < validPages.length; i++) {
            int page = validPages[i];
            pt.setFrameID(page, page * 10);
            if (clocks != null && i < clocks.length) {
                pt.getList().get(page).setClock(clocks[i]);
            }
        }
        return pt;
    }

    private static boolean testFIFO_Clocks() {
        PVMM_FIFO fifo = new PVMM_FIFO();
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, new int[]{25, 10, 18});
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(0);
        accesses.add(1);
        accesses.add(2);
        accesses.add(1);

        int victim = fifo.getVictim(accesses, pt);
        return victim == 1;
    }

    private static boolean testFIFO_EqualClocks_MemoryAccessOrder() {
        PVMM_FIFO fifo = new PVMM_FIFO();
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, new int[]{0, 0, 0});
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(2);
        accesses.add(0);
        accesses.add(1);

        int victim = fifo.getVictim(accesses, pt);
        return victim == 2;
    }

    private static boolean testLRU_Basic() {
        PVMM_LRU lru = new PVMM_LRU();
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, null);
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(0);
        accesses.add(1);
        accesses.add(2);
        accesses.add(0);
        accesses.add(1);

        int victim = lru.getVictim(accesses, pt);
        return victim == 2;
    }

    private static boolean testLRU_UnaccessedPage() {
        PVMM_LRU lru = new PVMM_LRU();
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, null);
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(0);
        accesses.add(1);
        accesses.add(0);

        int victim = lru.getVictim(accesses, pt);
        return victim == 2;
    }

    private static boolean testLFU_Basic() {
        PVMM_LFU lfu = new PVMM_LFU();
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, null);
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(0);
        accesses.add(1);
        accesses.add(1);
        accesses.add(2);
        accesses.add(2);
        accesses.add(2);

        int victim = lfu.getVictim(accesses, pt);
        return victim == 0;
    }

    private static boolean testLFU_TieBreakFIFO() {
        PVMM_LFU lfu = new PVMM_LFU();
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, new int[]{15, 8, 20});
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(0);
        accesses.add(1);
        accesses.add(2);
        accesses.add(2);
        accesses.add(2);

        int victim = lfu.getVictim(accesses, pt);
        return victim == 1;
    }

    private static boolean testMFU_Basic() {
        PVMM_MFU mfu = new PVMM_MFU();
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, null);
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(0);
        accesses.add(1);
        accesses.add(1);
        accesses.add(2);
        accesses.add(2);
        accesses.add(2);

        int victim = mfu.getVictim(accesses, pt);
        return victim == 2;
    }

    private static boolean testMFU_TieBreakFIFO() {
        PVMM_MFU mfu = new PVMM_MFU();
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, new int[]{15, 8, 20});
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(0);
        accesses.add(0);
        accesses.add(0);
        accesses.add(1);
        accesses.add(1);
        accesses.add(1);
        accesses.add(2);

        int victim = mfu.getVictim(accesses, pt);
        return victim == 1;
    }

    /**
     * Scenario where each algorithm selects a specific victim:
     * Valid pages: 0, 1, 2.
     * Clocks: Page 0 clock=10, Page 1 clock=20, Page 2 clock=30.
     * Accesses: [0, 1, 1, 0, 2, 0]
     * - Page 0: loaded at 10, last accessed at index 5, freq = 3
     * - Page 1: loaded at 20, last accessed at index 2, freq = 2
     * - Page 2: loaded at 30, last accessed at index 4, freq = 1
     *
     * FIFO -> Page 0 (lowest clock: 10)
     * LRU  -> Page 1 (accessed least recently: index 2)
     * LFU  -> Page 2 (lowest frequency: 1)
     * MFU  -> Page 0 (highest frequency: 3)
     */
    private static boolean testDifferentialScenario() {
        PageTable pt = createSamplePageTable(5, new int[]{0, 1, 2}, new int[]{10, 20, 30});
        LinkedList<Integer> accesses = new LinkedList<>();
        accesses.add(0);
        accesses.add(1);
        accesses.add(1);
        accesses.add(0);
        accesses.add(2);
        accesses.add(0);

        PVMM_FIFO fifo = new PVMM_FIFO();
        PVMM_LRU lru = new PVMM_LRU();
        PVMM_LFU lfu = new PVMM_LFU();
        PVMM_MFU mfu = new PVMM_MFU();

        int victimFIFO = fifo.getVictim(accesses, pt);
        int victimLRU = lru.getVictim(accesses, pt);
        int victimLFU = lfu.getVictim(accesses, pt);
        int victimMFU = mfu.getVictim(accesses, pt);

        System.out.println("Differential results: FIFO=" + victimFIFO + " LRU=" + victimLRU + " LFU=" + victimLFU + " MFU=" + victimMFU);

        return victimFIFO == 0 && victimLRU == 1 && victimLFU == 2 && victimMFU == 0;
    }
}
