#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>

#define MOST 100
#define MAX(x, y) (((x) > (y)) ? (x) : (y))

/* A claim that each task makes */
typedef struct Claim {
  int id;
  int claimUnits;
	int numAllocated;
} Claim;

/* Action for every instruction */
typedef enum Action {
	INITIATE, REQUEST, RELEASE, COMPUTE, TERMINATE
} Action;

/* State of a task */
typedef enum State {
	NORMAL, ABORTED, BLOCKED, UNBLOCKED, TERMINATED
} State;

/* Instruction to do an action, read in from command line */
typedef struct Instruction {
	Action action;
	int thirdNumber;  // Resource type or number of cycles
	int fourthNumber; // Initial claim, number requested, or number released
} Instruction;

/* A task to be run with optimistic and banker's algorithms */
typedef struct Task {
  int id;
  int maxClaimId;
	State state;
  Claim* claims[MOST];
  int cycleTerminated;
  int cyclesWaiting;
	Instruction* instructions[MOST];
	int numInstructions;
	int currentInstruction;
} Task;

/* Queue for blocked tasks */
typedef struct Queue {
	Task* blocked[MOST];
	int start;
	int end;
} Queue;

/* Resources available to be used by tasks */
typedef struct Resource {
  int id;
  int totalUnits;
	int unitsLeft;
	int unitsOnHold;
} Resource;

/* Manager to handle the running of tasks */
typedef struct Manager {
  Task* tasks[MOST];
  Resource* resources[MOST];
	Queue* queue;
  int cycle;
  int numTasks;
  int numResources;
} Manager;

/* Function Declarations */
void readRestOfFile(FILE* file, Manager* manager);
Manager* readFileInput(char* fileName);
void optimistic(Manager* manager);
bool enough(Manager* manager);
void performOptimisticInstructions(Manager* manager);
void performBankerInstructions(Manager* manager);
bool deadlock(Manager* m);
bool canRequest(Manager* manager, Task* t);
bool allDone(Manager* manager);
void unblockTasks(Manager* manager);
void releaseResources(Manager* manager);
bool isSafe(Manager* manager, Task* task);
bool checkSafetyForAll(Manager* copy);
void finishTask(Manager* copy, Task* curTask);
bool canFinish(Manager* copy, Task* curTask);
Manager* makeCopy(Manager* manager);
void abortTask(Manager* manager, Task* toAbort);

/* Global Variable */
bool verbose = false;

/* 
 * Read an input file, create a Manager, Tasks, Queue, and Resources
 */
Manager* readFileInput(char* fileName) {
	int temp = 1;
	int i;

  // Open file for read
  FILE* file = fopen(fileName, "r");
  if (!file) {
    printf("Cannot open file, please try again.");
    exit(1);
  }

	// Create manager to manage tasks and resources
  Manager* manager = (Manager*) malloc(sizeof(Manager));
  if (!manager) {
    printf("Cannot allocate manager.");
    exit(1);
  }

	// Initiate queue and set other variables
  fscanf(file, "%d %d", &manager->numTasks, &manager->numResources);
  manager->cycle = 0;
	Queue* queue = (Queue*) malloc(sizeof(Queue));
	manager->queue = queue;
	manager->queue->start = 0;
	manager->queue->end = 0;

  // Create resources 
  while (temp <= manager->numResources) {
    Resource* resource = (Resource*) malloc(sizeof(Resource));
    resource->id = temp;
    fscanf(file, "%d", &resource->totalUnits);
		resource->unitsLeft = resource->totalUnits;
		resource->unitsOnHold = 0;
    manager->resources[temp] = resource;
		temp++;
  }

	// Create tasks
	temp = 1;
	while (temp <= manager->numTasks) {
    Task* task = (Task*) malloc(sizeof(Task));
    task->id = temp;
		task->numInstructions = 0;
		task->state = NORMAL;
		task->currentInstruction = 1;
		task->maxClaimId = 0;
		for (i = 0; i < MOST; i++) {
			task->claims[i] = NULL;
		}
    manager->tasks[temp] = task;
    temp++;
	}
	readRestOfFile(file, manager);
  return manager;
}

/*
 * Read each instruction from file and save as an Instruction object.
 */
void readRestOfFile(FILE* file, Manager* manager) {
	char action[20];
	int i, j, k;
	
	while (!feof(file)) {
		fscanf(file, "%s%*[ \t] %d %d %d", &action, &i, &j, &k);
		Instruction* instruction = (Instruction*) malloc(sizeof(Instruction));
		instruction->thirdNumber = j;
		instruction->fourthNumber = k;

		if (strcmp(action, "initiate") == 0) {
			instruction->action = INITIATE; 
		} else if (strcmp(action, "request") == 0) {
			instruction->action = REQUEST; 
		} else if (strcmp(action, "release") == 0) {
			instruction->action = RELEASE;
		} else if (strcmp(action, "compute") == 0) {
			instruction->action = COMPUTE; 
		} else if (strcmp(action, "terminate") == 0) {
			instruction->action = TERMINATE; 
		} else {
			printf("Action not found: %s \n", action);
			exit(1);
		}
		int nInstructions = manager->tasks[i]->numInstructions;
		manager->tasks[i]->instructions[nInstructions + 1] = instruction;
		manager->tasks[i]->numInstructions++;
	}
}

/*
 * Algorithm to process tasks optimistically by granting resources
 * If they are available, otherwise blocking the task if it is not.
 * If the tasks are deadlocked, then it will abort the lowest
 * Numbered task.
 */
void optimistic(Manager* manager) {
	int i, j;

	while (!allDone(manager)) {
		int cycle = manager->cycle;
		if (verbose) {
			printf("Cycle %d: \n", cycle);
			printf("Checking blocked tasks...\n");
		}
		
		// Check blocked queue to see if any tasks can complete its request
		Queue* queue = manager->queue;
		for (i = queue->start; i < queue->end; i++) {
			if (queue->blocked[i] != NULL) {
				Task* t = queue->blocked[i];
				if (t->state == BLOCKED) {
					if (verbose) printf("Task %d is still blocked\n", t->id);
					t->cyclesWaiting++;
					if (canRequest(manager, t)) {
						t->state = UNBLOCKED;
						t->currentInstruction++;
						manager->queue->blocked[i] = NULL;
					}
				}
			}
		}

		// Perform the current operation for every task
		performOptimisticInstructions(manager);

		// Unblock all tasks that have been marked to run next cycle
		unblockTasks(manager);

		// If all runnable tasks are blocked waiting for resource, abort tasks 
		// Until a task is able to run
		if (deadlock(manager) && !allDone(manager)) {
			if (verbose) printf("All runnable tasks are blocked.\n");

			// Get the first blocked task with the lowest number
			for (i = 1; i <= manager->numTasks; i++) {
				if (manager->tasks[i]->state == BLOCKED) {
					Task* toAbort = manager->tasks[i];
					if (verbose) printf("Aborting task %d\n", toAbort->id);

					// Retrieve all resources that aborting task holds
					for (j = 1; j < toAbort->currentInstruction; j++) {
						Instruction* currentInstruct = toAbort->instructions[j];
						Resource* r = manager->resources[currentInstruct->thirdNumber];
						if (currentInstruct->action == REQUEST) {
							r->unitsLeft += currentInstruct->fourthNumber;
						} else if (currentInstruct->action == RELEASE) {
							r->unitsLeft -= currentInstruct->fourthNumber;
						}
					}

					// Mark as aborted and check if have enough resources to not deadlock
					toAbort->state = ABORTED;
					if (enough(manager)) {
						if (verbose) printf("There are enough resources to not abort tasks.\n");
						break;
					}
				}
			}
		}

		// Make units on hold released from this cycle available for next round
		releaseResources(manager);
		manager->cycle++;
		printf("\n");
	}		
}

/*
 * Banker's algorithm checks if a state is safe before granting resources
 * Will not deadlock because states will always be safe. If a process
 * requests more than its claims or makes a claim greater than the resources
 * available, it will be aborted.
 */
void bankers(Manager* manager) {
	int i, j;

	while (!allDone(manager)) {
		int cycle = manager->cycle;
		if (verbose) printf("Cycle %d: \n", cycle);
		
		if (verbose) printf("Checking blocked tasks...\n");
		Queue* queue = manager->queue;
		for (i = queue->start; i < queue->end; i++) {
			if (queue->blocked[i] != NULL) {
				Task* t = queue->blocked[i];
				if (t->state == BLOCKED) {
					if (verbose) printf("Task %d is still blocked\n", t->id);
					t->cyclesWaiting++;
					if (isSafe(manager, t)) {
						if (verbose) printf("Task %d is unblocked\n", t->id);
						t->state = UNBLOCKED;
						manager->queue->blocked[i] = NULL;
					}
				}
			}
		}

		performBankerInstructions(manager);
		unblockTasks(manager);

		// Release available resources for the next cycle
		releaseResources(manager);
		manager->cycle++;
		printf("\n");
	}	
}

/*
 * Check all tasks, if state is unblocked, change to normal
 */
void unblockTasks(Manager* manager) {
	int i;
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == UNBLOCKED) {
			if (verbose) printf("Task %d completes its request!\n", t->id);
			t->state = NORMAL;
		}
	}
}

/*
 * At the end of the cycle, release all resources on hold for next cycle
 */
void releaseResources(Manager* manager) {
	int i;
	for (i = 1; i <= manager->numResources; i++) {
		Resource* r = manager->resources[i];
		r->unitsLeft += r->unitsOnHold;
		r->unitsOnHold = 0;
	}
}

/*
 * Returns true and completes the request if the request leads to safe state,
 * Otherwise, return false and do nothing.
 */
bool isSafe(Manager* manager, Task* task) {
	int i;

	// Make a copy of the structs used in program so program can return to original state
	Manager* copy = makeCopy(manager);
	Task* taskCopy = copy->tasks[task->id];
	Instruction* instructCopy = taskCopy->instructions[taskCopy->currentInstruction];
	Claim* claimCopy = taskCopy->claims[instructCopy->thirdNumber];	

	// Error checking
	if (instructCopy->action != REQUEST) {
		if (verbose) printf("Current instruction's action must be request.\n");
		exit(1);
	}

	// If the request is greater than the initial claim, abort the task
	if (instructCopy->fourthNumber > claimCopy->claimUnits) {
		if (verbose) printf("Task %d requested more than its initial claim, aborting...\n", task->id);
		abortTask(manager, task);
		return false;
	}

	// If the combined allocated units are greater than the initial claim, abort task
	if (instructCopy->fourthNumber + claimCopy->numAllocated > claimCopy->claimUnits) {
		if (verbose) printf("Task %d request exceeds its claim; aborted.\n", task->id);
		abortTask(manager, task);
		return false;
	}

	// If there aren't enough units left of the resource to complete the request
	Resource* r = copy->resources[instructCopy->thirdNumber];
	if (instructCopy->fourthNumber > r->unitsLeft) {
		return false;
	}

	// Otherwise, try to grant the request and see if it leads to a safe state
	r->unitsLeft -= instructCopy->fourthNumber;
	claimCopy->numAllocated += instructCopy->fourthNumber;
	taskCopy->currentInstruction++;

	// If it leads to a safe state, actually complete the request on the original
	bool canRequest = checkSafetyForAll(copy);
	if (canRequest) {
		Instruction* toExecute = task->instructions[task->currentInstruction];
		Resource* resource = manager->resources[toExecute->thirdNumber];
		Claim* claim = task->claims[toExecute->thirdNumber];
		resource->unitsLeft -= toExecute->fourthNumber;
		claim->numAllocated += toExecute->fourthNumber;
		task->currentInstruction++;
	} 
	return canRequest;	
}

/*
 * Returns true if the tasks are in a safe state
 */ 
bool checkSafetyForAll(Manager* copy) {
	int i, j;
	bool keepGoing = true;

	// Try and finish all tasks to determine if safe is state
	while (keepGoing && !allDone(copy)) {
		keepGoing = false;

		// Check if every task can be finished
		for (i = 1; i <= copy->numTasks; i++) {
			Task* curTask = copy->tasks[i];
			if (curTask->state == NORMAL || curTask->state == BLOCKED) {
				if (canFinish(copy, curTask)) {
					finishTask(copy, curTask);
					keepGoing = true;
				}
			}
		}
	}
	return allDone(copy);
}

/*
 * Returns true if the task can be completed
 */
bool canFinish(Manager* copy, Task* curTask) {
	int i;

	for (i = 1; i <= curTask->maxClaimId; i++) {
		Claim* claim = curTask->claims[i];
		if (claim != NULL) {
			Resource* resource = copy->resources[i];
			int unitsNeeded = claim->claimUnits - claim->numAllocated;
			if (unitsNeeded > resource->unitsLeft) {
				return false;
			}
		}
	}
	return true;
}

/*
 * Finish the task and collect all its allocated resources
 */
void finishTask(Manager* copy, Task* curTask) {
	int i;

	for (i = 1; i <= curTask->maxClaimId; i++) {
		Claim* claim = curTask->claims[i];
		Resource* resource = copy->resources[i];
		resource->unitsLeft += claim->numAllocated;
		claim->numAllocated = 0;	
	}
	curTask->state = TERMINATED;
}

/*
 * Returns true if the current resources available are enough to end deadlock
 */
bool enough(Manager* manager) {
	int i;
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == BLOCKED) {
			Instruction* ins = t->instructions[t->currentInstruction];
			Resource* r = manager->resources[ins->thirdNumber];
			
			// Error check
			if (ins->action != REQUEST) {
				if (verbose) printf("Current instruction must be a request.");
				exit(1);
			}
			// There are enough resources available
			if (ins->fourthNumber <= r->unitsLeft) {
				return true;
			}
		}
	}
	return false;	
}

/*
 * Performs one instruction for optimistic manager
 */
void performOptimisticInstructions(Manager* manager) {
	int i;

	// Goes through every task and performs the current instruction
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == NORMAL) {
			Instruction* instruct = t->instructions[t->currentInstruction];
			Resource* r = manager->resources[instruct->thirdNumber];

			switch (instruct->action) {
				// Optimistic manager does not check initial claims
				case INITIATE:
					t->currentInstruction++;
					break;
				// Request can be fulfilled if the manager has the available resources
				// Otherwise, block the task until the resources are available
				case REQUEST:
					if (canRequest(manager, t)) {
						if (verbose) printf("Task %d completes its request.\n", t->id);
						t->currentInstruction++;
					} else {
						if (verbose) printf("Task %d is now blocked.\n", t->id);
						Queue* queue = manager->queue;
						queue->blocked[queue->end] = t;
						queue->end++;
						t->state = BLOCKED;
					}
					break;
				// Release the task and return all held resources
				case RELEASE:
					r->unitsOnHold += instruct->fourthNumber;
					t->currentInstruction++;
					if (verbose) printf("Task %d will release %d units of resource %d in next cycle\n", 
						t->id, r->unitsOnHold, instruct->thirdNumber);
					break;
				// Compute the task
				case COMPUTE:
					instruct->thirdNumber--;
					if (verbose) printf("Task % is computing...\n", t->id);
					if (instruct->thirdNumber <= 0) {
						t->state = NORMAL;
						t->currentInstruction++;
					}
					break;
				// Task is finished
				case TERMINATE:
					if (verbose) printf("Task %d has terminated.\n", t->id);
					t->state = TERMINATED;
					t->cycleTerminated = manager->cycle;
					break;
			}
		}
	}
}

/*
 * Performs one instruction for every task using Banker's algorithm
 */
void performBankerInstructions(Manager* manager) {
	int i;

	// Perform the current instruction for every task
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == NORMAL) {
			Instruction* instruct = t->instructions[t->currentInstruction];
			Resource* r = manager->resources[instruct->thirdNumber];

			switch (instruct->action) {
				// Initiate creates a Claim and checks that resources are enough
				case INITIATE:
					if (instruct->fourthNumber > r->totalUnits) {
						if (verbose) printf("Task %d claim exceeds number of resources, aborting...\n", t->id);
						t->state = ABORTED;
					} else {
						Claim* claim = (Claim*) malloc(sizeof(Claim));
						claim->id = instruct->thirdNumber;
						claim->claimUnits = instruct->fourthNumber;
						claim->numAllocated = 0;
						t->claims[claim->id] = claim;
						t->maxClaimId = MAX(t->maxClaimId, claim->id);
						t->currentInstruction++;
					}
					break;
				// Request is completed only if it leads to safe state, otherwise block
				case REQUEST:
					if (isSafe(manager, t)) {
						if (verbose) printf("Task %d completes its request.\n", t->id);
					} else {
						if (t->state == NORMAL) {
							if (verbose) printf("Task %d is now blocked.\n", t->id);
							Queue* queue = manager->queue;
							queue->blocked[queue->end] = t;
							queue->end++;
							t->state = BLOCKED;
						}
					}
					break;
				// Release resources for next cycle
				case RELEASE:
					r->unitsOnHold += instruct->fourthNumber;
					t->claims[instruct->thirdNumber]->numAllocated -= instruct->fourthNumber;
					t->currentInstruction++;
					if (verbose) printf("Task %d will release %d units of resource %d in next cycle\n", 
						t->id, r->unitsOnHold, instruct->thirdNumber);
					break;
				// Compute uses cycles
				case COMPUTE:
					instruct->thirdNumber--;
					if (verbose) printf("Task % is computing...\n", t->id);
					if (instruct->thirdNumber <= 0) {
						t->state = NORMAL;
						t->currentInstruction++;
					}
					break;
				// Finish the task
				case TERMINATE:
					if (verbose) printf("Task %d has terminated.\n", t->id);
					t->state = TERMINATED;
					t->cycleTerminated = manager->cycle;
					break;
			}
		}
	}
}

/*
 * Abort a task and return all held resources
 */
void abortTask(Manager* manager, Task* toAbort) {
	int i;

	toAbort->state = ABORTED;
	for (i = 1; i <= toAbort->maxClaimId; i++) {
		Claim* claim = toAbort->claims[i];
		if (claim != NULL) {
			Resource* resource = manager->resources[i];
			resource->unitsOnHold += claim->numAllocated;
			claim->numAllocated = 0;
		}
	}	
}

/*
 * Returns true if all tasks are deadlocked
 */
bool deadlock(Manager* m) {
	int i;
	for (i = 1; i <= m->numTasks; i++) {
		if (m->tasks[i]->state != BLOCKED && m->tasks[i]->state != TERMINATED
				&& m->tasks[i]->state != ABORTED) {
			return false;
		}
	}
	return true;
}

/*
 * Returns true if a request can be made, and completes the request.
 * Otherwise return false and do nothing.
 */
bool canRequest(Manager* manager, Task* t) {
	Instruction* instruct = t->instructions[t->currentInstruction];
	if (instruct->action != REQUEST) {
		if (verbose) printf("Current instruction must be request");
		exit(1);
	}
	
	Resource* r = manager->resources[instruct->thirdNumber];
	if (r->unitsLeft >= instruct->fourthNumber) {
		r->unitsLeft -= instruct->fourthNumber;
		return true;
	}
	return false;
}

/*
 * If all tasks are either terminated or aborted, the end states
 */
bool allDone(Manager* manager) {
	int i;
	for (i = 1; i <= manager->numTasks; i++) {
		if (manager->tasks[i]->state != ABORTED 
				&& manager->tasks[i]->state != TERMINATED) {
			return false;
		}
	}
	return true;
}

/*
 * Print the task numbers, cycles completed, cycles waiting, and percent
 */
void print(Manager* manager, char* runType) {
	int i;
	int totalCyclesRun = 0;
	int totalCyclesWaiting = 0;

	printf(runType);

	// Print for each task
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];

		if (t->state == TERMINATED) {
			int percent = (int) round((float)(t->cyclesWaiting * 100) / t->cycleTerminated);
			totalCyclesWaiting += t->cyclesWaiting;
			totalCyclesRun += t->cycleTerminated;
			printf("%s %d \t\t %d \t %d \t %d\%\n", 
				"Task",
				i, 
				t->cycleTerminated, 
				t->cyclesWaiting, 
				percent);
		} else if (t->state == ABORTED) {
			printf("%s %d \t\t %s\n", "Task", i, "aborted");
		}
	}

	// Print total
	printf("%s \t\t %d \t %d \t %d\%\n", 
		"Total",
		totalCyclesRun,
		totalCyclesWaiting,
		(int) round(((float)totalCyclesWaiting * 100) / totalCyclesRun));
}

/*
 * Make a copy of the manager for Banker's algorithm so we can return
 * To the original states
 */
Manager* makeCopy(Manager* manager) {
	int i, j;

	Manager* newManager = (Manager*) malloc(sizeof(Manager));
	newManager->cycle = manager->cycle;
	newManager->numTasks = manager->numTasks;
	newManager->numResources = manager->numResources;

	// Create new tasks
	for (i = 1; i <= manager->numTasks; i++) {
		Task* newTask = (Task*) malloc(sizeof(Task));
		Task* oldTask = manager->tasks[i];
		newTask->id = oldTask->id;
		newTask->maxClaimId = oldTask->maxClaimId;
		newTask->state = oldTask->state;
		newTask->cycleTerminated = oldTask->cycleTerminated;
		newTask->cyclesWaiting = oldTask->cyclesWaiting;
		newTask->numInstructions = oldTask->numInstructions;
		newTask->currentInstruction = oldTask->currentInstruction;

		// Create new claims
		for (j = 1; j <= oldTask->maxClaimId; j++) {
			if (oldTask->claims[j] == NULL) {
				newTask->claims[j] = NULL;
			} else {
				Claim* newClaim = (Claim*) malloc(sizeof(Claim));
				Claim* oldClaim = oldTask->claims[j];
				newClaim->id = oldClaim->id;
				newClaim->claimUnits = oldClaim->claimUnits;
				newClaim->numAllocated = oldClaim->numAllocated;
				newTask->claims[j] = newClaim;
			}
		}

		// Create new instructions
		for (j = 1; j <= oldTask->numInstructions; j++) {
			Instruction* oldInstruct = oldTask->instructions[j];
			Instruction* newInstruct = (Instruction*) malloc(sizeof(Instruction));
			newInstruct->action = oldInstruct->action;
			newInstruct->thirdNumber = oldInstruct->thirdNumber;
			newInstruct->fourthNumber = oldInstruct->fourthNumber;
			newTask->instructions[j] = newInstruct;	
		}		
		newManager->tasks[i] = newTask;
	}

	// Create new resources
	for (i = 1; i <= manager->numResources; i++) {
		Resource* resource = (Resource*) malloc(sizeof(Resource));
		Resource* oldResource = manager->resources[i];
		resource->id = oldResource->id;
		resource->totalUnits = oldResource->totalUnits;
		resource->unitsLeft = oldResource->unitsLeft;
		resource->unitsOnHold = oldResource->unitsOnHold;
		newManager->resources[i] = resource;
	}

	// Create new queue
	newManager->queue = (Queue*) malloc(sizeof(Queue));
	for (i = 0; i < manager->queue->end; i++) {
		newManager->queue->blocked[i] = manager->queue->blocked[i];
	}
	newManager->queue->start = manager->queue->start;
	newManager->queue->end = manager->queue->end;

	return newManager;
}

/*
 * Frees all allocated resources of the program
 */
void freeEverything(Manager* manager) {
	int i, j;

	for (i = 1; i <= manager->numTasks; i++) {
		for (j = 1; j <= manager->tasks[i]->maxClaimId; j++) {
			if (manager->tasks[i]->claims[j] != NULL) {
				free(manager->tasks[i]->claims[j]);	
			}
		}
		for (j = 1; j <= manager->tasks[i]->numInstructions; j++) {
			free(manager->tasks[i]->instructions[j]);
		}
		free(manager->tasks[i]);
	}
	for (i = 1; i <= manager->numResources; i++) {
		free(manager->resources[i]);
	}
	free(manager->queue);
	free(manager);
}

/*****************************************************************************/

int main(int argc, char* argv[]) {
  if (argc < 2) {
    printf("Enter the name of the input file as an argument.\n");
    exit(1);
  }

	if (argc == 3 && strcmp(argv[2], "--verbose") == 0) {
		verbose = true;
	}

	// Read the file
  Manager* optimisticManager = readFileInput(argv[1]);
	Manager* bankerManager = readFileInput(argv[1]);

	// Perform the process of tasks
	optimistic(optimisticManager);
	bankers(bankerManager);

	printf("*-------------------------------------------------------------*\n\n");

	// Print out result
	print(optimisticManager, "FIFO\n");
	printf("\n");
	print(bankerManager, "BANKER'S\n");

	// Free allocated resources
	freeEverything(optimisticManager);	
	freeEverything(bankerManager);
	
	printf("\n\n");
  return 0;
}
