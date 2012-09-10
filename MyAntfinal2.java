import ants.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyAnt implements Ant{
        
    private Direction lastDir = null;
    
    private boolean foundFood = false;    
    private boolean isCarryingFood = false;
    
    // Patience value is a behavioral trait of the ant, ie how prone it is to change directions.
    private int maxPatience = 0; 
    private int currPatience = 0;
    
    //If the ant has exceeded maxIdle amount of steps without accomplishing anything, change its behavior
    private int maxIdle = 30;  
    private int currentIdle = 0;
    private int behaviorType = 0;
    
    // Used to save the routes taken by the ant
    private Stack<Direction> steps = new Stack<Direction>();
    private Stack<Direction> savedSteps = new Stack<Direction>();
    
    // Used for protecting access to the route data structures
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
        
    // Current position - the mound is at 0,0
    private int currX = 0;
    private int currY = 0;
    
    
    // Constructor
    public MyAnt()
    {
    	    	
    	// Start with patience value of one
    	
	    maxPatience = 1;
	    
	    // Pick an initial direction to move in
	    Random randomGenerator = new Random();
	    int randomInt = randomGenerator.nextInt(4);
	    if(randomInt == 0 )
	    {
	    	lastDir = Direction.NORTH;
	    			    	
	    }
	    if(randomInt == 1 )
	    {
	    	lastDir = Direction.EAST;
	    		    	
	    }
	    if(randomInt == 2 )
	    {
	    	lastDir = Direction.SOUTH;
	    		    	
	    }
	    if(randomInt == 3 )
	    {
	    	lastDir = Direction.WEST;
	    		    	
	    }
	    
    }
    
	public Action getAction(Surroundings surroundings){
						
		w.lock();
		try
		{
		if(currX == 0 && currY == 0 && isCarryingFood == true)
		{
			// We have returned to the nest with food, drop it off
			isCarryingFood = false;
			lastDir = null;
			currentIdle = 0;
					
			if(maxPatience < 20)
				maxPatience++;
		    
		    if(savedSteps.empty())
		    {
		    	//Set a new random direction to head off to
		    	Random randomGenerator = new Random();
			    int randomInt = randomGenerator.nextInt(4);
			    if(randomInt == 0 )
			    {
			    	lastDir = Direction.NORTH;
			    			    	
			    }
			    if(randomInt == 1 )
			    {
			    	lastDir = Direction.EAST;
			    		    	
			    }
			    if(randomInt == 2 )
			    {
			    	lastDir = Direction.SOUTH;
			    		    	
			    }
			    if(randomInt == 3 )
			    {
			    	lastDir = Direction.WEST;
			    		    	
			    }
		    	
		    }		    
		    		    
			return Action.DROP_OFF;
			
		}
		
		
		
		// Check if we are on a tile that has food, if so gather up the food
		if(surroundings.getCurrentTile().getAmountOfFood()>0 && isCarryingFood == false && !(currX == 0 && currY ==0))
		{
			lastDir = null;			
			isCarryingFood = true;
			currentIdle = 0;
			
			if(!savedSteps.empty())
				savedSteps.clear();
			
			if(surroundings.getCurrentTile().getAmountOfFood() > 1)
			{
				foundFood = true;				
			}
			else
			{
				foundFood = false;				
				
			}			
						
			return Action.GATHER;
		}
		
		// Look around and move to the spot that has food
		if(surroundings.getTile(Direction.NORTH).isTravelable() && surroundings.getTile(Direction.NORTH).getAmountOfFood() > 0 
				&& !isCarryingFood && savedSteps.empty() && !(currX==0 && currY==-1))
		{
			lastDir = Direction.NORTH;
			UpdateXY(lastDir);
			steps.push(lastDir);
			return Action.move(lastDir);			
		}
		
		if(surroundings.getTile(Direction.SOUTH).isTravelable() && surroundings.getTile(Direction.SOUTH).getAmountOfFood() > 0 
				&& !isCarryingFood && savedSteps.empty() && !(currX==0 && currY==1))
		{
			lastDir = Direction.SOUTH;
			UpdateXY(lastDir);
			steps.push(lastDir);
			return Action.move(lastDir);			
		}
		
		if(surroundings.getTile(Direction.EAST).isTravelable() && surroundings.getTile(Direction.EAST).getAmountOfFood() > 0 
				&& !isCarryingFood && savedSteps.empty() && !(currX==-1 && currY==0))
		{
			lastDir = Direction.EAST;
			UpdateXY(lastDir);
			steps.push(lastDir);
			return Action.move(lastDir);			
		}
		
		if(surroundings.getTile(Direction.WEST).isTravelable() && surroundings.getTile(Direction.WEST).getAmountOfFood() > 0 
				&& !isCarryingFood && savedSteps.empty() && !(currX==1 && currY==0))
		{
			lastDir = Direction.WEST;
			UpdateXY(lastDir);
			steps.push(lastDir);
			return Action.move(lastDir);			
		}
		
		
		// We're not carrying food and we haven't found any food...				
		// Keep trying to find some food.
		if(isCarryingFood == false && (!foundFood && savedSteps.empty()))
		{
			return GoNext(surroundings);						
						
		}
		   
					
		// Check if we are trying to reach known food location, or return to mound with food.				
		// If so use our saved routes to either return to the mound, or return to the food location.
		if(isCarryingFood == true)
		{
			// Reverse our steps to get back to mound, save the steps to the other stack						
			Direction dir = steps.pop();
						
			savedSteps.push(dir);
			
			dir = GetOpposite(dir);			
			UpdateXY(dir);
			return Action.move(dir);
		}
		else if(!savedSteps.empty())
		{
			// Keep following steps back to food, push the steps to the other stack
			Direction dir = savedSteps.pop();
						
			steps.push(dir);
			
			UpdateXY(dir);
			return Action.move(dir);			
			
		}
		
		// Check to see if all the food from this tile has been consumed
		if(foundFood && savedSteps.empty())
		{			
				if(surroundings.getCurrentTile().getAmountOfFood() == 0)
				{
					// No more food left here
					foundFood = false;
					
					// Pick a new direction to move in
					Random randomGenerator = new Random();
				    int randomInt = randomGenerator.nextInt(4);
				    if(randomInt == 0 )
				    {
				    	lastDir = Direction.NORTH;
				    			    	
				    }
				    if(randomInt == 1 )
				    {
				    	lastDir = Direction.EAST;
				    		    	
				    }
				    if(randomInt == 2 )
				    {
				    	lastDir = Direction.SOUTH;
				    		    	
				    }
				    if(randomInt == 3 )
				    {
				    	lastDir = Direction.WEST;
				    		    	
				    }
				}
								
		}
								
						
		return Action.HALT;
		
		}
		finally
		{
			w.unlock();
		}
	
	}
	
	
	
	public byte[] send(){
		
		// We share valuable information with other ants, ie the path back to the mound
		Direction[] dirlist;
		byte[] target;
		
		r.lock();
		try
		{
			
			target = new byte[steps.size()];
			
			dirlist = new Direction[steps.size()];
			steps.toArray(dirlist);
			
			for(int i = 0; i < steps.size(); i++)
			{
				if(dirlist[i] == Direction.NORTH)
					target[i] = 1;
				else if(dirlist[i] == Direction.SOUTH)
					target[i] = 2;
				else if(dirlist[i] == Direction.EAST)
					target[i] = 3;
				else if(dirlist[i] == Direction.WEST)
					target[i] = 4;
				else target[i] = 0;
				
			}
			
		}
		finally
		{
			r.unlock();
		}
		
				
		return target;
		
		
	}
	
	public void receive(byte[] data){		
			
		// If we have received a shorter route to get back to mound, overwrite our saved route.
		// Have to synchronize access, since multiple ants can be simultaneously sending us messages.
		w.lock();
		try
		{
			
			if(data.length < steps.size())
			{
				steps.clear();
				
				for(int i = 0; i< data.length ; i++)
				{
					switch(data[i])
					{
					case 1: 
						steps.push(Direction.NORTH);
						break;
					case 2:
						steps.push(Direction.SOUTH);
						break;
					case 3:
						steps.push(Direction.EAST);
						break;
					case 4:
						steps.push(Direction.WEST);
						break;
					default:
							break;
					}
				}
						
			}
				
		}
		finally
		{
			w.unlock();
		}
					
		
	}
	
	
	// Helper function to update our current coordinate
	private void UpdateXY(Direction d)
	{
		lastDir = d;
		if(d == Direction.NORTH)
			currY++;
		if(d == Direction.SOUTH)
			currY--;
		if(d == Direction.EAST)
			currX++;
		if(d == Direction.WEST)
			currX--;		
		
	}
	
	
	private Direction GetOpposite(Direction dir)
	{
		if(dir == Direction.NORTH)
			return Direction.SOUTH;
		if(dir == Direction.SOUTH)
			return Direction.NORTH;
		if(dir == Direction.EAST)
			return Direction.WEST;
		if(dir == Direction.WEST)
			return Direction.EAST;
		else return null;
	}
	
	// Search for food, using some simple heuristics
	private Action GoNext(Surroundings surroundings)
	{
		// See if we can keep going in same direction
		if(lastDir!=null && surroundings.getTile(lastDir).isTravelable() && currPatience <= maxPatience )
		{
			currPatience++;
			UpdateXY(lastDir);
			steps.push(lastDir);
			return Action.move(lastDir);
			
		}
		// If we hit a wall or run out of patience, we need to change direction
		else
		{
			if(currPatience > maxPatience)
				currPatience = 0;
			
			currentIdle++;
			// Change ant's turning behavior if nothing has happened for too long, to prevent ants getting stuck in a loop
			if(currentIdle > maxIdle)
			{				
				maxPatience = 2;
				
				if(behaviorType ==0)
					behaviorType = 1;
				else if(behaviorType ==1)
					behaviorType = 0;
				currentIdle = 0;
			}
						
			Direction dir;
			
			// only backtrack as last resort, make sure we don't get stuck in circles
			if(behaviorType == 0)
			{
				if(lastDir == Direction.NORTH)
			    {
					if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						dir = Direction.WEST;
						
					} else 
					if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						dir = Direction.EAST;
						
					}
					else
						if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						dir = Direction.SOUTH;						
						
					}
						else dir = Direction.NORTH;
					
			    }
				
				else if(lastDir == Direction.SOUTH)
			    {
					if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						dir = Direction.EAST;
						
					} else 
					if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						dir = Direction.WEST;
						
					}
					else if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						dir = Direction.NORTH;
						
						
					}
					else dir = Direction.SOUTH;
					
			    }
			    	
				else if(lastDir == Direction.EAST)
			    {
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						dir = Direction.SOUTH;
						
					} else 
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						dir = Direction.NORTH;
						
					}
					else if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						dir = Direction.WEST;
						
						
					}
					else dir = Direction.EAST;
					
			    }
				
				else if (lastDir == Direction.WEST)
			    {
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						dir = Direction.NORTH;
						
					} 
					else 
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						dir = Direction.SOUTH;
						
					}
					else if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						dir = Direction.EAST;
						
						
					}
					else dir = Direction.WEST;
					
			    }
				else 
			    {
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						dir = Direction.NORTH;
						
					} 
					else 
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						dir = Direction.SOUTH;
						
					}
					else if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						dir = Direction.EAST;
						
						
					}
					else dir = Direction.WEST;
					
			    }
			}
			else
			{
				if(lastDir == Direction.NORTH)
			    {
					if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						dir = Direction.EAST;
						
					} else 
					if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						dir = Direction.WEST;
						
					}
					else
						if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						dir = Direction.SOUTH;
						
						
					}
						else dir = Direction.NORTH;
					
			    }
				
				else if(lastDir == Direction.SOUTH)
			    {
					if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						dir = Direction.WEST;
						
					} else 
					if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						dir = Direction.EAST;
						
					}
					else if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						dir = Direction.NORTH;
						
						
					}
					else dir = Direction.SOUTH;
					
			    }
			    	
				else if(lastDir == Direction.EAST)
			    {
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						dir = Direction.NORTH;
						
					} else 
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						dir = Direction.SOUTH;
						
					}
					else if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						dir = Direction.WEST;
						
						
					}
					else dir = Direction.EAST;
					
			    }
				
				else if (lastDir == Direction.WEST)
			    {
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						dir = Direction.SOUTH;
						
					} else 
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						dir = Direction.NORTH;
						
					}
					else if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						dir = Direction.EAST;
						
						
					}
					else dir = Direction.WEST;
					
			    }
				else 
			    {
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						dir = Direction.SOUTH;
						
					} else 
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						dir = Direction.NORTH;
						
					}
					else if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						dir = Direction.WEST;
						
						
					}
					else dir = Direction.EAST;
					
			    }
				
							
				
			}
			
			UpdateXY(dir);
			steps.push(dir);
			return Action.move(dir);
						    
		   
		}
		
	}
	
	

}