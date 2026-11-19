/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.virtualmemory;

import java.util.LinkedList;
import ur_os.memory.paging.PageTable;
import ur_os.memory.paging.PageTableEntry;

/**
 * First-In, First-Out (FIFO) page replacement algorithm.
 * Selects the page that has been in memory the longest (earliest load time).
 *
 * @author user
 */
public class PVMM_FIFO extends ProcessVirtualMemoryManager {

    public PVMM_FIFO() {
        type = ProcessVirtualMemoryManagerType.FIFO;
    }

    @Override
    public int getVictim(LinkedList<Integer> memoryAccesses, PageTable pt) {
        if (pt == null) {
            if (memoryAccesses != null && !memoryAccesses.isEmpty()) {
                return memoryAccesses.getFirst();
            }
            return -1;
        }

        LinkedList<Integer> validListPages = new LinkedList<>();
        int i = 0;
        for (PageTableEntry pte : pt.getList()) {
            if (pte.isValid()) {
                validListPages.add(i);
            }
            i++;
        }

        if (validListPages.isEmpty()) {
            return -1;
        }

        if (validListPages.size() == 1) {
            return validListPages.getFirst();
        }

        int victim = validListPages.getFirst();
        int minClock = Integer.MAX_VALUE;
        boolean clockDifferentiates = false;
        int firstClock = pt.getList().get(validListPages.getFirst()).getClock();

        for (int page : validListPages) {
            PageTableEntry pte = pt.getList().get(page);
            if (pte.getClock() != firstClock) {
                clockDifferentiates = true;
            }
            if (pte.getClock() < minClock) {
                minClock = pte.getClock();
                victim = page;
            }
        }

        if (clockDifferentiates) {
            return victim;
        }

        // If clocks do not differentiate (e.g., in test cases where clock was not explicitly set),
        // select the valid page that appeared earliest in memoryAccesses
        if (memoryAccesses != null && !memoryAccesses.isEmpty()) {
            for (int page : memoryAccesses) {
                if (validListPages.contains(page)) {
                    return page;
                }
            }
        }

        return validListPages.getFirst();
    }
}
