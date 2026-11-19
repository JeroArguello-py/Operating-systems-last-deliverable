package ur_os.memory.freememorymagament;

public class NextFitMemorySlotManager extends FreeMemorySlotManager {

    private int lastBase = 0; // address where the last allocation ended

    public NextFitMemorySlotManager(int memSize) {
        super(memSize);
    }

    @Override
    public MemorySlot getSlot(int size) {
        int n = this.list.size();
        if (n == 0) return null;

        // First free slot whose base is >= the cursor (wrap around to the beginning if none is found)
        int start = 0;
        while (start < n && this.list.get(start).getBase() < lastBase) {
            start++;
        }
        if (start == n) start = 0;

        // Circular search
        for (int k = 0; k < n; k++) {
            int i = (start + k) % n;
            MemorySlot s = this.list.get(i);

            if (s.getSize() >= size) {
                MemorySlot allocated = new MemorySlot(s.getBase(), size);
                int rest = s.getSize() - size;

                this.list.remove(i);
                if (rest > 0) {
                    this.list.add(i, new MemorySlot(s.getBase() + size, rest));
                }
                lastBase = s.getBase() + size; // the cursor advances
                return allocated;
            }
        }
        return null; // no free slot is large enough
    }

    public static void main(String[] args) {
        NextFitMemorySlotManager m = new NextFitMemorySlotManager(1100);
        MemorySlot a = m.getSlot(400);
        MemorySlot b = m.getSlot(300);
        MemorySlot c = m.getSlot(200);
        MemorySlot d = m.getSlot(100);

        m.returnMemorySlot(new MemorySlot(a.getBase(), a.getSize()));
        m.returnMemorySlot(new MemorySlot(c.getBase(), c.getSize()));
        // Free slots: [0,400], [700,200], [1000,100]

        MemorySlot r1 = m.getSlot(400); // exact fit at 0, cursor -> 400
        m.returnMemorySlot(new MemorySlot(r1.getBase(), r1.getSize()));
        // Free slots again: [0,400], [700,200], [1000,100], cursor at 400

        MemorySlot r2 = m.getSlot(50);
        System.out.println("Base para 50: " + r2.getBase());
        // Next Fit => 700 (continues from the cursor). First Fit => 0.
    }
}