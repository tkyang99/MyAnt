import ants.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyAnt implements Ant{

    private static int id = 0;
    private int idnum;
    private Direction lastDir = null;
    
    private boolean foundFood = false;
    //private int foundFoodY = 0;
    private boolean isCarryingFood = false;
    private int maxPatience = 0; 
    private int currPatience = 0;
    
    //If the ant has exceeded this amount of steps without accomplishing anything, change its behavior
    private int maxIdle = 50;  
    private int currentIdle = 0;
    private int behaviorType = 0;
    private Stack<Direction> steps = new Stack<Direction>();
    private Stack<Direction> savedSteps = new Stack<Direction>();
    
 // Used for protecting access to the stack data structures
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
        
    // Current position - the nest is at 0,0
    private int currX = 0;
    private int currY = 0;
    
    
    
    public MyAnt()
    {
    	// Increment ID to identify this ant    	
    	idnum = id++;
    	
    	// Create random patience value. This way each ant behaves differently.
    	Random randomGenerator = new Random();
	    maxPatience = randomGenerator.nextInt(15)+5;
	    
	    // Pick an initial direction to move in
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
					
			Random randomGenerator = new Random();
		    maxPatience = randomGenerator.nextInt(15)+5;
		    
		    if(savedSteps.empty())
		    {
		    	//Set a new random direction to head off to
		    	// Pick an initial direction to move in
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
		
		//If we are at the nest and don't know where the food is, wait a bit for an ant with food to return and tell us
		//the location before wandering off on our own.
		if(currX == 0 && currY == 0 && isCarryingFood == false && idnum > 8 
				&& savedSteps.empty() && currentIdle < 10)
		{
			//currentIdle++;
			//return Action.HALT;
		}
		
		// Check if we are on a tile that has food
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
				//If there's food remaining, remember this food location
				//foundFoodY = currY;
				//foundFoodX = currX;
			}
			else
			{
				foundFood = false;
				//foundFoodY = 0;
				//foundFoodX = 0;
				
			}
			
						
			return Action.GATHER;
		}
		
		// Move to the spot that has food
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
		
		// We're not carrying food and we haven't found any food.				
		// Keep trying to find some food.
		if(isCarryingFood == false && (!foundFood && savedSteps.empty()))
		{
			return GoNext(surroundings);						
						
		}
		   
					
		// We are trying to reach known food location, or return to nest with food		
				
		
		
		
		// Keep moving in the right direction if we can
		if(isCarryingFood == true)
		{
			// Reverse our steps to get back to mound, save the steps to the other stack
			if(steps.size() == 0)
				System.out.println("AAAUGGGH");
			
			Direction dir = steps.pop();
			if(!surroundings.getTile(GetOpposite(dir)).isTravelable())
				System.out.println("AAAUGGGH");
			
			// Only save the steps if there's still food to be picked up
			savedSteps.push(dir);
			
			dir = GetOpposite(dir);			
			UpdateXY(dir);
			return Action.move(dir);
		}
		else if(!savedSteps.empty())
		{
			// Keep following steps back to food, push the steps to the other stack
			Direction dir = savedSteps.pop();
			if(!surroundings.getTile(dir).isTravelable())
				System.out.println("AAAUGGGH");
			
			steps.push(dir);
			
			UpdateXY(dir);
			return Action.move(dir);			
			
		}
		
		// Check to see if all the food from this tile has been consumed
		if(foundFood && savedSteps.empty())
		{			
				if(surroundings.getCurrentTile().getAmountOfFood() == 0)
				{
					foundFood = false;
					// Pick a new initial direction to move in
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
		
		// We share the most valuable information with other ants, ie the shortest path back to the mound!
		Direction[] dirlist;
		byte[] target;
		
		r.lock();
		try
		{
			if(currX == 0 && currY == 0 && steps.empty() && foundFood && !savedSteps.empty())
			{
				target = new byte[savedSteps.size()];
				
				dirlist = new Direction[savedSteps.size()];
				savedSteps.toArray(dirlist);
				
				for(int i = 0; i < savedSteps.size(); i++)
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
			else	
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
		}
		finally
		{
			r.unlock();
		}
		
		
		
		
		return target;
		
		
	}
	
	public void receive(byte[] data){
		// If we haven't found any food, or if there's closer food, get food location from the other ant
		/*int newDistance = Math.abs((int)data[0]) + Math.abs((int)data[1]);
		int origDistance = Math.abs((int)foundFoodX) + Math.abs((int)foundFoodY);
		
		if(foundFoodX == 0 && foundFoodY == 0 || newDistance < origDistance)
		{
		//	foundFoodX = (int)data[0];
		//	foundFoodY = (int)data[1];
			
		}*/
		if(currX == 0 && currY == 0 && savedSteps.empty() && data.length > 0)
		{
			
			w.lock();
			try
			{
				// We have received data on where food may be	
					
				foundFood = true;	
				for(int i = 0; i< data.length; i++)
				{
					switch(data[i])
					{
					case 1: 
						savedSteps.push(Direction.NORTH);
						break;
					case 2:
						savedSteps.push(Direction.SOUTH);
						break;
					case 3:
						savedSteps.push(Direction.EAST);
						break;
					case 4:
						savedSteps.push(Direction.WEST);
						break;
						default:
							break;
					}
				}
				
			}
			finally
			{
				w.unlock();
			}
		
			return;
			
		}
			
		// We have received a shorter route to get back to mound, use this instead.
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
	
	// Choose the next direction to move in
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
			// Change ant's turning behavior if nothing has happened for too long
			if(currentIdle > maxIdle)
			{				
				maxPatience = 5;
				
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