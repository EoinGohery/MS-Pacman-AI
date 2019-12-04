package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

import static pacman.game.Constants.*;

/*
 * This controller utilises 3 tactics, in order of importance:
 * 1. Get away from any non-edible ghost that is in close proximity
 * 2. Go after the nearest edible ghost
 * 3. Go to the nearest pill/power pill
 */
public class MyPacManV0 extends Controller<MOVE>
{	
	private static final int MIN_DISTANCE=6; //if a ghost is this close, run away
	private Random rnd=new Random();
	private int[] powerPills;
	
	public MOVE getMove(Game game,long timeDue)
	{			
		int current=game.getPacmanCurrentNodeIndex();
		int[] pills=game.getPillIndices();
		powerPills=game.getPowerPillIndices();
		int minDistance=Integer.MAX_VALUE;
		MOVE potentialMove = null;
		GHOST minGhost=null;	
		if (game.getCurrentLevelTime()<47) 
		{
			return goToPowerPill(current,game);
		}
		
		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE) {
					minGhost=ghost;
					if (isCrowded(game) && averageGhostDistance(game)<40) 
					{
						return goToPowerPill(current, game);
					}
				}
			}
		}
		if(minGhost!=null)
					return game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(minGhost), DM.PATH);
				
		//Strategy 2: find the nearest edible ghost and go after them	
		minGhost =null;
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)>20)
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		
		if(minGhost!=null)	//we found an edible ghost
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.PATH);
		
		//Strategy 3: go after the pills and power pills
		ArrayList<Integer> targets=new ArrayList<Integer>();
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(game.isPillStillAvailable(i))
				targets.add(pills[i]);
		
		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);				
					
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
				
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
					
		//return the next direction once the closest target has been identified
		return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
	}
	
    private boolean isCrowded(Game game)
    {
    	GHOST[] ghosts=GHOST.values();
        float distance=0;
        
        for (int i=0;i<ghosts.length-1;i++)
            for(int j=i+1;j<ghosts.length;j++)
                distance+=game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghosts[i]),game.getGhostCurrentNodeIndex(ghosts[j]));
        return (distance/6)<20 ? true : false;
    }
    
    private boolean isMoveSafe(int current,Game game, MOVE move) {
		GHOST minGhost=null;	
		GHOST min2Ghost=null;
		GHOST min3Ghost=null;
		GHOST min4Ghost=null;
		int minDistance=Integer.MAX_VALUE;
		for(GHOST l : GHOST.values()) {
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(l));
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=l;
				}
		}
    	for(GHOST i : GHOST.values()) {
			if (i !=minGhost) 
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(i));
				minDistance=Integer.MAX_VALUE;
				if(distance<minDistance)
				{
					minDistance=distance;
					min2Ghost=i;
				}
			}
		}
		for(GHOST j : GHOST.values()) {
			if (j != min2Ghost && j!= minGhost) 
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(j));
				minDistance=Integer.MAX_VALUE;
				if(distance<minDistance)
				{
					minDistance=distance;
					min3Ghost=j;
				}
			}
		}
		for(GHOST k : GHOST.values()) {
			if (k != minGhost && k!= min2Ghost && k !=min3Ghost) 
				min4Ghost=k;
			}
		if (game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(minGhost), DM.PATH)!=move && 
			game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(min2Ghost), DM.PATH)!=move && 
			game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(min3Ghost), DM.PATH)!=move &&
			game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(min4Ghost), DM.PATH)!=move )
//			&& game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(minGhost))<5 
//			&& game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(min2Ghost))<10 
//			&& game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(min3Ghost))<15 
//			&& game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(min4Ghost))<15)
			return true;
			else return false;
    }
    public int averageGhostDistance(Game game) {
    	int total=0;
    	for(GHOST ghost : GHOST.values()) 
    	{
    		total=+game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost));
    	}
    	return total/4;
    }
    
	public MOVE goToPowerPill(int current, Game game) {
		int nearestPowerPill=0;
		MOVE nearPillPath = null;
		int powerDistance = Integer.MAX_VALUE;
		for(int i=0;i<powerPills.length;i++) 
		{
			if(game.isPowerPillStillAvailable(i) && game.getShortestPathDistance(current,powerPills[i])<powerDistance) 
			{
				powerDistance=game.getShortestPathDistance(current,powerPills[i]);
				nearestPowerPill = i;
			}
		}
		nearPillPath = game.getNextMoveTowardsTarget(current,powerPills[nearestPowerPill],DM.PATH);
			for(GHOST ghost : GHOST.values()) 
			{
				if (game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.PATH) ==nearPillPath &&game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) 
				{
					return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.PATH);
				}
			}
			int totalLairTime=0;
			for(GHOST ghost : GHOST.values()) {
				totalLairTime=+game.getGhostLairTime(ghost);
			}
			if (totalLairTime==0) {
				return nearPillPath;
			}
		return game.getPacmanLastMoveMade();
	}
}

