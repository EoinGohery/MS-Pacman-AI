package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import static pacman.game.Constants.*;

public class MyPacMan extends Controller<MOVE>
{
	private MOVE[] possibleMoves;
	private int[] moveScore;
	
	public MOVE getMove(Game game,long timeDue)
	{			
		int current=game.getPacmanCurrentNodeIndex();
		//System.out.println(current);
		possibleMoves = game.getPossibleMoves(current);
		int[] pills=game.getPillIndices();
		int[] powerPills=game.getPowerPillIndices();
		int score=0;
    	int ghostDistance =0;
		ArrayList<Integer> targets=new ArrayList<Integer>();
    	moveScore = new int[possibleMoves.length];
		
    	for (int i=0; i<possibleMoves.length; i++) {
    		for (GHOST ghost : GHOST.values()) {
    			if (game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
    				ghostDistance=game.getShortestPath(game.getNeighbour(current, possibleMoves[i]), game.getNeighbour(game.getGhostCurrentNodeIndex(ghost),game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getNeighbour(current, possibleMoves[i]), game.getGhostLastMoveMade(ghost), DM.PATH)), possibleMoves[i]).length;
    				if (ghostDistance<10) {
    					score+=200;
    				} else if (ghostDistance<=15) {
    					score+=150;
    				} else if (ghostDistance<=25) {
    					score+=30;
    				} else if (ghostDistance<=80) {
    					score+=5;
    				}
    			}
    		}
    		moveScore[i]=score;
    		score =0;
    	}
    	possibleMoves = sortMoves(game);
    	if (moveScore[0]>100) {
    		return possibleMoves[0];	
    	}
    			
//    	score =0;
//    	for (int i=0; i<possibleMoves.length; i++) {	
//    		for (GHOST ghost : GHOST.values()) {
//    			if (game.getGhostEdibleTime(ghost)>2) {
//    				ghostDistance=game.getShortestPath(game.getNeighbour(current, possibleMoves[i]), game.getNeighbour(game.getGhostCurrentNodeIndex(ghost),game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getNeighbour(current, possibleMoves[i]), game.getGhostLastMoveMade(ghost), DM.MANHATTAN)), possibleMoves[i]).length;
//    				if (ghostDistance<5) {
//    					score+=10;
//    				} else if (ghostDistance<=20) {
//    					score+=20;
//    				} else if (ghostDistance<=30) {
//    					score+=60;
//    				} else if (ghostDistance<=100) {
//    					score+=100;
//    				}
//    			}
//    		}
//    		moveScore[i]=score;
//    		score =0;
//    	}
//    	possibleMoves = sortMoves(game);
//    	if (moveScore[0]<200)
//    	{
//    		return possibleMoves[0];	
//    	}
    	
    	
		for(int i=0;i<pills.length;i++)								
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);
		for(int i=0;i<powerPills.length;i++)			
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);	
		int[] targetsArray=new int[targets.size()];	
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);

		
    	for (int i=0; i<possibleMoves.length; i++) {
    			if (goToPowerPill(game,current,powerPills)!=null && goToPowerPill(game,current,powerPills)==possibleMoves[i]) {
    				int totalLairTime=0;
    				int totalEdibleTime=0;
    				for(GHOST ghost2 : GHOST.values()) 
    				{
    					totalLairTime+=game.getGhostLairTime(ghost2);
    					totalEdibleTime+=game.getGhostEdibleTime(ghost2);
    				}
    				if (totalLairTime==0 && totalEdibleTime==0) {
    					score =10;
    				}
    			}
    			moveScore[i]=score;
    			score =0;
    	}
    	possibleMoves = sortMoves(game); 
    	if (moveScore[possibleMoves.length-1]==10) {
    		return possibleMoves[possibleMoves.length-1];
    	}
    	
    	return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
	}
	
	private int getValue(Game game, MOVE move) {
		for (int i=0; i<possibleMoves.length; i++) {
			if (move==possibleMoves[i]) {
				return moveScore[i];
			}
		}
		return 1000;
	}
    
    private MOVE[] sortMoves(Game game) {
    	MOVE[] sortedMoves = new MOVE[possibleMoves.length];
    	int[] sortedValues = moveScore.clone();
    	Arrays.sort(sortedValues);
    		for (int i=0; i<sortedValues.length; i++)
    		{
    			for (int j=0; j<moveScore.length; j++) {
    				if (moveScore[j]==sortedValues[i]) {
    					sortedMoves[i]=possibleMoves[j];
    				}
    			}
    		}
    	moveScore=sortedValues.clone();
		return sortedMoves;
    }
    
	public MOVE goToPowerPill(Game game, int current, int[]powerPills) {
		int nearestPowerPill=10000;
		int powerDistance = Integer.MAX_VALUE;
		for(int i=0;i<powerPills.length;i++) 
		{
			if(game.isPowerPillStillAvailable(i) && game.getShortestPathDistance(current,powerPills[i])<powerDistance) 
			{
				powerDistance=game.getShortestPathDistance(current,powerPills[i]);
				nearestPowerPill = i;
			}
		}
		if (nearestPowerPill!=10000) {
			return  game.getNextMoveTowardsTarget(current,powerPills[nearestPowerPill],DM.PATH);
		}
		return null;
	}
}


