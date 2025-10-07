package me.itzmojahide.boundcore.objects;

public enum Bound {
    NATURE("NatureBound", Rarity.NORMAL),
    WIND("WindBound", Rarity.NORMAL),
    STONE("StoneBound", Rarity.NORMAL),
    INFERNO("InfernoBound", Rarity.EPIC),
    THUNDER("ThunderBound", Rarity.EPIC),
    SHADOW("ShadowBound", Rarity.EPIC),
    FROST("FrostBound", Rarity.EPIC),
    AQUA("AquaBound", Rarity.EPIC),
    DIVINE("DivineBound", Rarity.LEGENDARY),
    VOID("VoidBound", Rarity.LEGENDARY),
    CHRONO("ChronoBound", Rarity.LEGENDARY),
    ADMIN("AdminBound", Rarity.ADMIN);

    private final String name;
    private final Rarity rarity;

    Bound(String name, Rarity rarity) { this.name = name; this.rarity = rarity; }
    public String getName() { return name; }
    public Rarity getRarity() { return rarity; }

    public static Bound fromString(String name) {
        for (Bound b : Bound.values()) { if (b.getName().equalsIgnoreCase(name)) return b; }
        return null;
    }
    public enum Rarity { NORMAL, EPIC, LEGENDARY, ADMIN }
}
