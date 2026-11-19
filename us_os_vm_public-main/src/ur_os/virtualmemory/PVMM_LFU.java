/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.virtualmemory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import ur_os.memory.paging.PageTable;
import ur_os.memory.paging.PageTableEntry;

/**
 * Least Frequently Used (LFU) page replacement algorithm.
 * Selects the page with the smallest reference count.
 * Ties are broken using FIFO (earliest loading clock), then LRU.
 *
 * @author user
 */
public class PVMM_LFU extends ProcessVirtualMemoryManager {

    public PVMM_LFU() {
        type = ProcessVirtualMemoryManagerType.LFU;
    }

    @Override
    public int getVictim(LinkedList<Integer> memoryAccesses, PageTable pt) {
        if (pt == null) {
            if (memoryAccesses == null || memoryAccesses.isEmpty()) {
                return -1;
            }
            Map<Integer, Integer> freqMap = new HashMap<>();
            for (int page : memoryAccesses) {
                freqMap.put(page, freqMap.getOrDefault(page, 0) + 1);
            }
            int minCount = Integer.MAX_VALUE;
            int victim = -1;
            for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
                if (entry.getValue() < minCount) {
                    minCount = entry.getValue();
                    victim = entry.getKey();
                }
            }
            return victim;
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

        // Count frequency of references for each valid page
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int vp : validListPages) {
            freqMap.put(vp, 0);
        }

        if (memoryAccesses != null) {
            for (int page : memoryAccesses) {
                if (freqMap.containsKey(page)) {
                    freqMap.put(page, freqMap.get(page) + 1);
                }
            }
        }

        // Find minimum frequency
        int minCount = Integer.MAX_VALUE;
        for (int vp : validListPages) {
            int count = freqMap.get(vp);
            if (count < minCount) {
                minCount = count;
            }
        }

        // Find all candidates with the minimum frequency
        LinkedList<Integer> candidates = new LinkedList<>();
        for (int vp : validListPages) {
            if (freqMap.get(vp) == minCount) {
                candidates.add(vp);
            }
        }

        if (candidates.size() == 1) {
            return candidates.getFirst();
        }

        // Tie-breaker 1: FIFO (earliest loaded clock)
        int victim = candidates.getFirst();
        int minClock = Integer.MAX_VALUE;
        boolean clockDifferentiates = false;
        int firstClock = pt.getList().get(candidates.getFirst()).getClock();

        for (int candidate : candidates) {
            int c = pt.getList().get(candidate).getClock();
            if (c != firstClock) {
                clockDifferentiates = true;
            }
            if (c < minClock) {
                minClock = c;
                victim = candidate;
            }
        }

        if (clockDifferentiates) {
            return victim;
        }

        // Tie-breaker 2: LRU among tied candidates
        LinkedList<Integer> recentCandidates = new LinkedList<>();
        if (memoryAccesses != null) {
            for (int idx = memoryAccesses.size() - 1; idx >= 0; idx--) {
                int page = memoryAccesses.get(idx);
                if (candidates.contains(page) && !recentCandidates.contains(page)) {
                    recentCandidates.add(page);
                }
            }
        }

        for (int candidate : candidates) {
            if (!recentCandidates.contains(candidate)) {
                return candidate;
            }
        }

        if (!recentCandidates.isEmpty()) {
            return recentCandidates.getLast();
        }

        return candidates.getFirst();
    }
}
