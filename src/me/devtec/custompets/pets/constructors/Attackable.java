package me.devtec.custompets.pets.constructors;

public interface Attackable extends Damageable, Pet {
    public boolean isAttacking();

    public void setAggresive(boolean status);

    public boolean isAggresive();
}
