package pacman.entries.pacman;

import java.util.ArrayList;

import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/*
 * This controller utilizes 4 tactics, in order of importance:
 * 	1. Get away from any non-edible ghost that is in close proximity
 * 	2. Go to powerPill
 * 		only if safe
 * 	3. Go after the nearest edible ghost
 * 		only if safe and within timer
 * 	4. Go to the nearest pill
 */
public class Group03_Pacman extends Controller<MOVE>
{	
	private static final int MIN_DISTANCE=30;
	private int[] powerPills;
	
	public MOVE getMove(Game game,long timeDue)
	{			
		int current=game.getPacmanCurrentNodeIndex();
		int[] pills=game.getPillIndices();
		powerPills=game.getPowerPillIndices();
		int minDistance=Integer.MAX_VALUE;
		MOVE potentialMove = null;
		GHOST minGhost=null;	
		if (game.getTotalTime()<25) { // waits in center at start to prevent early contact with ghosts
			return game.getNextMoveTowardsTarget(current, game.getGhostInitialNodeIndex(), DM.EUCLID);
		}
		
		//Strategy 1: If any non-edible ghost is too close run away
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE) {
					minGhost=ghost;
				}
			}
		}
		//If there is a safe route to a PowerPill, go to nearest PowerPill
		if (goToPowerPill(current, game)!=null) 
		{
			return goToPowerPill(current, game);
		//If not then Avoid Ghosts
		} else if(minGhost!=null) {
			potentialMove = game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(minGhost), DM.PATH);
			for(GHOST ghost : GHOST.values()) 
			{
				if (ghost!=minGhost) {
					if (game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.PATH) ==potentialMove 
						&&game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0 
						&& game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost))<10) {
							return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.PATH);
					}
				}
			}
			return game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(minGhost), DM.PATH);	
		}
		
		//Strategy 2: Find the nearest edible ghost and go after them under powerpill effect	
		minGhost =null;
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)>5)
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		// Check if move towards ghost is safe. if not avoid ghosts and go to strategy 3
		if(minGhost!=null) {
			potentialMove = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.PATH); 
				if (isMoveSafe(game, current, potentialMove )<150) {
					return potentialMove;
				}
		}
		

		//Strategy 3: go after the available pills and power pills
		ArrayList<Integer> targets=new ArrayList<Integer>();
		for(int i=0;i<pills.length;i++)							
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);
		
		for(int i=0;i<powerPills.length;i++)			
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);				
					
		int[] targetsArray=new int[targets.size()];		
				
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
					
		//Go to the closest pill that has been identified
		return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
	}
	
    // Checks the potential move to see how many ghosts are in that direction and how close they are.
    // Assigns a dangerValue based on direction and distance of ghosts
    private int isMoveSafe(Game game, int current, MOVE move) {
    	int dangerValue=0;
    	int ghostDistance =0;
    	for (GHOST ghost : GHOST.values()) {
    		if (game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
   				ghostDistance=game.getShortestPath(game.getNeighbour(current, move), game.getNeighbour(game.getGhostCurrentNodeIndex(ghost),
   				game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), 
   				game.getNeighbour(current, move), game.getGhostLastMoveMade(ghost), DM.PATH)), move).length;
   				if (ghostDistance<10) {
   					dangerValue+=200;
   				} else if (ghostDistance<=20) {
   					dangerValue+=150;
   				} else if (ghostDistance<=50) {
   					dangerValue+=80;
   				} else if (ghostDistance<=80) {
    				dangerValue+=10;
    			}
    			if (move==game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(ghost), DM.PATH)&&ghostDistance<6) {
    				dangerValue+=50;
   				} else if (0<game.getGhostLairTime(ghost)&&game.getGhostLairTime(ghost)<3) {
   				ghostDistance=game.getShortestPath(game.getNeighbour(current, move), game.getGhostInitialNodeIndex(), move).length;
   					if (ghostDistance<10) {
  						dangerValue+=100;
    				}
    			}
    		} else if (game.getGhostEdibleTime(ghost)>3) {
    			ghostDistance=game.getShortestPath(game.getNeighbour(current, move), game.getNeighbour(game.getGhostCurrentNodeIndex(ghost),
    			game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getNeighbour(current, move), 
    			game.getGhostLastMoveMade(ghost), DM.MANHATTAN)), move).length;
    			if (ghostDistance<5) {
    				dangerValue-=150;
    			} else if (ghostDistance<=20) {
    				dangerValue-=100;
    			} else if (ghostDistance<=30) {
    				dangerValue-=50;
    			} else if (ghostDistance<=70) {
    				dangerValue-=20;
   				}
   			}
   		}
   		return dangerValue; 
   	}
    
    
    // Finds the path towards the nearest PowerPill and return move if move is safe to make
	public MOVE goToPowerPill(int current, Game game) {
		int nearestPowerPill=0;
		MOVE nearPillPath = null;
		int powerPillDistance = Integer.MAX_VALUE;
		for(int i=0;i<powerPills.length;i++) 
		{
			if(game.isPowerPillStillAvailable(i) && game.getShortestPathDistance(current,powerPills[i])<powerPillDistance) 
			{
				powerPillDistance=game.getShortestPathDistance(current,powerPills[i]);
				nearestPowerPill = i;
			}
		}
		nearPillPath = game.getNextMoveTowardsTarget(current,powerPills[nearestPowerPill],DM.PATH);	
		int totalLairTime=0;
		int totalEdibleTime=0;
		for(GHOST ghost : GHOST.values()) {
			totalLairTime+=game.getGhostLairTime(ghost);
			totalEdibleTime+=game.getGhostEdibleTime(ghost);
    	}
    	if (totalLairTime==0 && totalEdibleTime==0 && isMoveSafe(game, current, nearPillPath)<250) {
			return nearPillPath;
		} else
			return null;
	}
}

