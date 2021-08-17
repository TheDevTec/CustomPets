package me.devtec.custompets.pets.constructors;

public interface Defendable extends Damageable, Pet {
	public boolean isAttacking();
	
	public void setDefending(boolean status);
	
	public boolean isDefending();
	
	public void setDefendingOwner(boolean status);
	
	public boolean isDefendingOwner();
}
