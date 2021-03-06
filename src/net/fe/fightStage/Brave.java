package net.fe.fightStage;

import net.fe.unit.Unit;

public class Brave extends CombatTrigger{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2833222257001214176L;

	public Brave(){
		super(NO_NAME_MOD, YOUR_TURN_POST);
	}

	@Override
	public boolean attempt(Unit user, int range) {
		return true;
	}

	@Override
	public void runPostAttack(CombatCalculator calc, boolean dir, Unit a, Unit d,
			int damage, String currentEffect) {
		if(d.getHp() > 0 && currentEffect.equals("None")){
			calc.addAttack("Brave");
		}
	}
	
	public CombatTrigger getCopy(){
		return new Brave();
	}
}
