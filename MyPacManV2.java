package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

import static pacman.game.Constants.*;

public class MyPacManV2 extends Controller<MOVE>
{
	private int[] powerPills;
	private int[] junctions;
	private MOVE[] possibleMoves;
	private int[] safeValues;
	private int current;
	private int[] targetsArray;
	
	public MOVE getMove(Game game,long timeDue)
	{			
		
		current=game.getPacmanCurrentNodeIndex();
		int[] pills=game.getPillIndices();
		powerPills=game.getPowerPillIndices();
		junctions=game.getJunctionIndices();
		
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		for(int i=0;i<pills.length;i++)								
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);
		
		for(int i=0;i<powerPills.length;i++)			
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);				
					
		targetsArray=new int[targets.size()];		
				
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		possibleMoves = game.getPossibleMoves(current);
		safeValues = sortSafeValues(game);
		possibleMoves = sortMoves(game);
		
		return possibleMoves[0];
	}
	
		
	
    private boolean isCrowded(Game game)
    {
    	GHOST[] ghosts=GHOST.values();
        float distance=0;
        
        for (int i=0;i<ghosts.length-1;i++)
            for(int j=i+1;j<ghosts.length;j++)
                distance+=game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghosts[i]),game.getGhostCurrentNodeIndex(ghosts[j]));
        return (distance/6)<80 ? true : false;
    }
    
    private int[] sortSafeValues(Game game) {
    	int dangerValue=0;
    	int ghostDistance =0;
    	int[] moveValues = new int[possibleMoves.length];
    	for (int i=0; i<possibleMoves.length; i++) {
    		for (GHOST ghost : GHOST.values()) {
    			if (game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
    				ghostDistance=game.getShortestPath(game.getNeighbour(current, possibleMoves[i]), game.getNeighbour(game.getGhostCurrentNodeIndex(ghost),game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getNeighbour(current, possibleMoves[i]), game.getGhostLastMoveMade(ghost), DM.PATH)), possibleMoves[i]).length;
    				if (ghostDistance<5) {
    					dangerValue+=203;
    				} else if (ghostDistance<=15) {
    					dangerValue+=101;
    				} else if (ghostDistance<=25) {
    					dangerValue+=34;
    				} else if (ghostDistance<=50) {
    					dangerValue+=5;
    				}
    				if (possibleMoves[i]==game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(ghost), DM.PATH)&&ghostDistance<6) {
    					dangerValue+=2;
        			}

    			} else if (0<game.getGhostLairTime(ghost)&&game.getGhostLairTime(ghost)<3) {
    				ghostDistance=game.getShortestPath(game.getNeighbour(current, possibleMoves[i]), game.getGhostInitialNodeIndex(), possibleMoves[i]).length;
    				if (ghostDistance<40) {
    					dangerValue+=11;
    				}
    			}
    			else if (game.getGhostEdibleTime(ghost)>0) {
    				ghostDistance=game.getShortestPath(game.getNeighbour(current, possibleMoves[i]), game.getNeighbour(game.getGhostCurrentNodeIndex(ghost),game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getNeighbour(current, possibleMoves[i]), game.getGhostLastMoveMade(ghost), DM.MANHATTAN)), possibleMoves[i]).length;
    				if (ghostDistance<5) {
    					dangerValue-=157;
    				} else if (ghostDistance<=20) {
    					dangerValue-=123;
    				} else if (ghostDistance<=30) {
    					dangerValue-=61;
    				} else if (ghostDistance<=70) {
    					dangerValue-=10;
    				}
    			}
    		}
    		if (game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.MANHATTAN),DM.PATH)==possibleMoves[i])
    		{
    			if (targetsArray.length<20) {
    				dangerValue -= 82;
    			} else if (targetsArray.length<70) {
    				dangerValue -= 41;
    			} else {
    				dangerValue -= 2;
    			}
    		}
    		if (game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,junctions,DM.MANHATTAN),DM.PATH)==possibleMoves[i])
    		{
    			dangerValue -= 1;
    		}
    		if (game.isJunction(game.getNeighbour(current, possibleMoves[i]))&&possibleMoves[i]!=game.getPacmanLastMoveMade().opposite()) 
    		{
    			dangerValue -= 2;
    		}
    		if (game.getPacmanLastMoveMade()==possibleMoves[i])
    		{
    			dangerValue -= 1;
    		}
    		if (goToPowerPill(game)==possibleMoves[i] && isCrowded(game)) {
    		
    			int totalLairTime=0;
    			int totalEdibleTime=0;
    			for(GHOST ghost2 : GHOST.values()) 
    			{
    				totalLairTime+=game.getGhostLairTime(ghost2);
    				totalEdibleTime+=game.getGhostEdibleTime(ghost2);
    			}
    			if (totalLairTime==0 && totalEdibleTime==0) 
    			{
    				dangerValue -=30;
    		}
    		}
    		moveValues[i]=dangerValue;
    		dangerValue=0;
    	}
    	return moveValues;	
    }
    
    private MOVE[] sortMoves(Game game) {
    	MOVE[] sortedMoves = new MOVE[game.getPossibleMoves(current).length];
    	int[] sortedValues = safeValues.clone();
    	Arrays.sort(sortedValues);
    		for (int i=0; i<sortedValues.length; i++)
    		{
    			for (int j=0; j<safeValues.length; j++) {
    				if (safeValues[j]==sortedValues[i]) {
    					sortedMoves[i]=possibleMoves[j];
    				}
    			}
    		}
    	safeValues=sortedValues.clone();
		return sortedMoves;
    }
        
    public int averageGhostDistance(Game game) {
    	int total=0;
    	for(GHOST ghost : GHOST.values()) 
    	{
    		total=+game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost));
    	}
    	return total/4;
    }
    
	public MOVE goToPowerPill(Game game) {
		int nearestPowerPill=1000;
		int powerDistance = Integer.MAX_VALUE;
		for(int i=0;i<powerPills.length;i++) 
		{
			if(game.isPowerPillStillAvailable(i) && game.getShortestPathDistance(current,powerPills[i])<powerDistance) 
			{
				powerDistance=game.getShortestPathDistance(current,powerPills[i]);
				nearestPowerPill = i;
			}
		}
		if (nearestPowerPill!=1000) {
			return  game.getNextMoveTowardsTarget(current,powerPills[nearestPowerPill],DM.PATH);
		}
		return null;
	}
}


