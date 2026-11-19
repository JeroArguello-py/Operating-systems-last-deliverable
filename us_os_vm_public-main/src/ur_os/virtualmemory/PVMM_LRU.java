/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.virtualmemory;

import java.util.LinkedList;
import ur_os.memory.paging.PageTable;
import ur_os.memory.paging.PageTableEntry;

/**
 * Least Recently Used (LRU) page replacement algorithm.
 * Selects the page that has not been referenced for the longest period of time.
 *
 * @author user
 */
public class PVMM_LRU extends ProcessVirtualMemoryManager {

    public PVMM_LRU() {
        type = ProcessVirtualMemoryManagerType.LRU;
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

        if (memoryAccesses == null || memoryAccesses.isEmpty()) {
            return validListPages.getFirst();
        }

        LinkedList<Integer> pages = new LinkedList<>();
        int size = memoryAccesses.size() - 1;
        int temp;
        while (size >= 0 && pages.size() < validListPages.size()) {
            temp = memoryAccesses.get(size);
            if (!pages.contains(temp) && validListPages.contains(temp)) {
                pages.add(temp);
            }
            size--;
        }

        // Valid pages that have never been accessed are the least recently used
        for (int validPage : validListPages) {
            if (!pages.contains(validPage)) {
                return validPage;
            }
        }

        return pages.getLast();
    }
}
