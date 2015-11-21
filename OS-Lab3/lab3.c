#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>

#define MAX 100

typedef struct Claim {
  int id;
  int claimUnits;
	int numAllocated;
} Claim;

typedef enum Action {
	INITIATE, REQUEST, RELEASE, COMPUTE, TERMINATE
} Action;

typedef enum State {
	NORMAL, ABORTED, BLOCKED, UNBLOCKED, TERMINATED
} State;

typedef struct Instruction {
	Action action;
	int thirdNumber;  // Resource type or number of cycles
	int fourthNumber; // Initial claim, number requested, or number released
} Instruction;

typedef struct Task {
  int id;
  int numClaims;
	State state;
  Claim* claims[MAX];
  int cycleTerminated;
  int cyclesWaiting;
	Instruction* instructions[MAX];
	int numInstructions;
	int currentInstruction;
} Task;

typedef struct Queue {
	Task* blocked[MAX];
	int start;
	int end;
} Queue;

typedef struct Resource {
  int id;
  int totalUnits;
	int unitsLeft;
	int unitsOnHold;
} Resource;

typedef struct Manager {
  Task* tasks[MAX];
  Resource* resources[MAX];
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

Manager* readFileInput(char* fileName) {
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
  int temp = 1;
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
    manager->tasks[temp] = task;
    temp++;
	}
	readRestOfFile(file, manager);
  return manager;
}

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

void optimistic(Manager* manager) {
	int i, j;

	while (!allDone(manager)) {
		int cycle = manager->cycle;
		printf("Cycle %d: \n", cycle);		

		printf("Checking blocked tasks...\n");
		Queue* queue = manager->queue;
		for (i = queue->start; i < queue->end; i++) {
			if (queue->blocked[i] != NULL) {
				Task* t = queue->blocked[i];
				if (t->state == BLOCKED) {
					printf("Task %d is still blocked\n", t->id);
					t->cyclesWaiting++;
					if (canRequest(manager, t)) {
						t->state = UNBLOCKED;
						t->currentInstruction++;
						manager->queue->blocked[i] = NULL;
					}
				}
			}
		}

		performOptimisticInstructions(manager);
		unblockTasks(manager);

		// If all runnable tasks are blocked waiting for resource, abort tasks until
		// A task is able to run
		if (deadlock(manager) && !allDone(manager)) {
			printf("All runnable tasks are blocked.\n");
			for (i = 1; i <= manager->numTasks; i++) {
				if (manager->tasks[i]->state == BLOCKED) {
					Task* toAbort = manager->tasks[i];
					printf("Aborting task %d\n", toAbort->id);
					for (j = 1; j < toAbort->currentInstruction; j++) {
						Instruction* currentInstruct = toAbort->instructions[j];
						Resource* r = manager->resources[currentInstruct->thirdNumber];
						if (currentInstruct->action == REQUEST) {
							r->unitsLeft += currentInstruct->fourthNumber;
						} else if (currentInstruct->action == RELEASE) {
							r->unitsLeft -= currentInstruct->fourthNumber;
						}
					}
					toAbort->state = ABORTED;
					if (enough(manager)) {
						printf("There are enough resources to not abort tasks.\n");
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

void unblockTasks(Manager* manager) {
	int i;
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == UNBLOCKED) {
			printf("Task %d completes its request!\n", t->id);
			t->state = NORMAL;
		}
	}
}

void releaseResources(Manager* manager) {
	int i;
	for (i = 1; i <= manager->numResources; i++) {
		Resource* r = manager->resources[i];
		r->unitsLeft += r->unitsOnHold;
		r->unitsOnHold = 0;
	}
}

void bankers(Manager* manager) {
	int i, j;

	while (!allDone(manager)) {
		int cycle = manager->cycle;
		printf("Cycle %d: \n", cycle);
		
		printf("Checking blocked tasks...\n");
		Queue* queue = manager->queue;
		for (i = queue->start; i < queue->end; i++) {
			if (queue->blocked[i] != NULL) {
				Task* t = queue->blocked[i];
				if (t->state == BLOCKED) {
					printf("Task %d is still blocked\n", t->id);
					t->cyclesWaiting++;
					if (isSafe(manager, t)) {
						t->state = UNBLOCKED;
						t->currentInstruction++;
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

bool isSafe(Manager* manager, Task* task) {
	int i;
	Manager* copy = makeCopy(manager);
	Task* taskCopy = copy->tasks[task->id];
	Instruction* currInstruct = taskCopy->instructions[taskCopy->currentInstruction];
	Claim* claim = taskCopy->claims[currInstruct->thirdNumber];	

	if (currInstruct->action != REQUEST) {
		printf("Current instruction's action must be request.\n");
		exit(1);
	}

	if (currInstruct->fourthNumber > claim->claimUnits) {
		printf("Task %d requested more than its initial claim, aborting...\n");
		task->state = ABORTED;
		return false;
	}

	Resource* r = copy->resources[currInstruct->thirdNumber];
	if (currInstruct->fourthNumber > r->unitsLeft) {
		printf("Task %d request cannot be granted, not enough resources available.\n");
		return false;
	}

	r->unitsLeft -= currInstruct->fourthNumber;
	taskCopy->currentInstruction++;
	return checkSafetyForAll(copy);	
}

bool checkSafetyForAll(Manager* copy) {
	int i, j;
	bool keepGoing = true;

	while (keepGoing && !allDone(copy)) {
		keepGoing = false;
		for (i = 1; i <= copy->numTasks; i++) {
			Task* curTask = copy->tasks[i];
			if (canFinish(copy, curTask)) {
				finishTask(copy, curTask);
				keepGoing = true;
			}
		}
	}
	return allDone(copy);
}

bool canFinish(Manager* copy, Task* curTask) {
	int i;

	for (i = 1; i <= curTask->numClaims; i++) {
		Claim* claim = curTask->claims[i];
		Resource* resource = copy->resources[i];
		int unitsNeeded = claim->claimUnits - claim->numAllocated;
		if (unitsNeeded > resource->unitsLeft) {
			return false;
		}
	}
	return true;
}

void finishTask(Manager* copy, Task* curTask) {
	int i;

	for (i = 1; i < curTask->numClaims; i++) {
		Claim* claim = curTask->claims[i];
		Resource* resource = copy->resources[i];
		resource->unitsLeft += claim->numAllocated;
		claim->numAllocated = 0;	
	}
	curTask->state = TERMINATED;
}

bool enough(Manager* manager) {
	int i;
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == BLOCKED) {
			Instruction* ins = t->instructions[t->currentInstruction];
			Resource* r = manager->resources[ins->thirdNumber];
			if (ins->action != REQUEST) {
				printf("Current instruction must be a request.");
				exit(1);
			}
			if (ins->fourthNumber <= r->unitsLeft) {
				return true;
			}
		}
	}
	return false;	
}

void performOptimisticInstructions(Manager* manager) {
	int i;
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == NORMAL) {
			Instruction* instruct = t->instructions[t->currentInstruction];
			Resource* r = manager->resources[instruct->thirdNumber];

			switch (instruct->action) {
				case INITIATE:
					t->currentInstruction++;
					break;
				case REQUEST:
					if (canRequest(manager, t)) {
						printf("Task %d completes its request.\n", t->id);
						t->currentInstruction++;
					} else {
						printf("Task %d is now blocked.\n", t->id);
						Queue* queue = manager->queue;
						queue->blocked[queue->end] = t;
						queue->end++;
						t->state = BLOCKED;
					}
					break;
				case RELEASE:
					r->unitsOnHold += instruct->fourthNumber;
					t->currentInstruction++;
					printf("Task %d will release %d units of resource %d in next cycle\n", 
						t->id, r->unitsOnHold, instruct->thirdNumber);
					break;
				case COMPUTE:
					instruct->thirdNumber--;
					printf("Task % is computing...\n", t->id);
					if (instruct->thirdNumber <= 0) {
						t->state = NORMAL;
						t->currentInstruction++;
					}
					break;
				case TERMINATE:
					printf("Task %d has terminated.\n", t->id);
					t->state = TERMINATED;
					t->cycleTerminated = manager->cycle;
					break;
			}
		}
	}
}

void performBankerInstructions(Manager* manager) {
	int i;

	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == NORMAL) {
			Instruction* instruct = t->instructions[t->currentInstruction];
			Resource* r = manager->resources[instruct->thirdNumber];

			switch (instruct->action) {
				case INITIATE:
					if (instruct->fourthNumber > r->totalUnits) {
						printf("Task %d claim exceeds number of resources, aborting...\n", t->id);
						t->state = ABORTED;
					} else {
						Claim* claim = (Claim*) malloc(sizeof(Claim));
						claim->id = instruct->thirdNumber;
						claim->claimUnits = instruct->fourthNumber;
						claim->numAllocated = 0;
						t->currentInstruction++;
					}
					break;
				case REQUEST:
					if (isSafe(manager, t)) {
						printf("Task %d completes its request.\n", t->id);
						t->currentInstruction++;
					} else {
						printf("Task %d is now blocked.\n", t->id);
						Queue* queue = manager->queue;
						queue->blocked[queue->end] = t;
						queue->end++;
						t->state = BLOCKED;
					}
					break;
				case RELEASE:
					r->unitsOnHold += instruct->fourthNumber;
					t->currentInstruction++;
					printf("Task %d will release %d units of resource %d in next cycle\n", 
						t->id, r->unitsOnHold, instruct->thirdNumber);
					break;
				case COMPUTE:
					instruct->thirdNumber--;
					printf("Task % is computing...\n", t->id);
					if (instruct->thirdNumber <= 0) {
						t->state = NORMAL;
						t->currentInstruction++;
					}
					break;
				case TERMINATE:
					printf("Task %d has terminated.\n", t->id);
					t->state = TERMINATED;
					t->cycleTerminated = manager->cycle;
					break;
			}
		}
	}
}

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

bool canRequest(Manager* manager, Task* t) {
	Instruction* instruct = t->instructions[t->currentInstruction];
	if (instruct->action != REQUEST) {
		printf("Current instruction must be request");
		exit(1);
	}
	
	Resource* r = manager->resources[instruct->thirdNumber];
	if (r->unitsLeft >= instruct->fourthNumber) {
		r->unitsLeft -= instruct->fourthNumber;
		return true;
	}
	return false;
}

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

void printOptimistic(Manager* manager) {
	int i;
	int totalCyclesRun = 0;
	int totalCyclesWaiting = 0;

	printf("FIFO\n");
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];

		if (t->state == TERMINATED) {
			int percent = (int) ceil((float)(t->cyclesWaiting * 100) / t->cycleTerminated);
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
	printf("%s \t\t %d \t %d \t %d\%\n", 
		"Total",
		totalCyclesRun,
		totalCyclesWaiting,
		(int) ceil(((float)totalCyclesWaiting * 100) / totalCyclesRun));
}

Manager* makeCopy(Manager* manager) {
	int i, j;

	Manager* newManager = (Manager*) malloc(sizeof(Manager));
	newManager->cycle = manager->cycle;
	newManager->numTasks = manager->numTasks;
	newManager->numResources = manager->numResources;

	for (i = 1; i <= manager->numTasks; i++) {
		Task* newTask = (Task*) malloc(sizeof(Task));
		Task* oldTask = manager->tasks[i];
		newTask->id = oldTask->id;
		newTask->numClaims = oldTask->numClaims;
		newTask->state = oldTask->state;
		newTask->cycleTerminated = oldTask->cycleTerminated;
		newTask->cyclesWaiting = oldTask->cyclesWaiting;
		newTask->numInstructions = oldTask->numInstructions;
		newTask->currentInstruction = oldTask->currentInstruction;
		for (j = 1; j <= oldTask->numClaims; j++) {
			Claim* newClaim = (Claim*) malloc(sizeof(Claim));
			Claim* oldClaim = oldTask->claims[j];
			newClaim->id = oldClaim->id;
			newClaim->claimUnits = oldClaim->claimUnits;
			newClaim->numAllocated = oldClaim->numAllocated;
			newTask->claims[j] = newClaim;
		}
		newManager->tasks[i] = newTask;
	}

	for (i = 1; i <= manager->numResources; i++) {
		Resource* resource = (Resource*) malloc(sizeof(Resource));
		Resource* oldResource = manager->resources[i];
		resource->id = oldResource->id;
		resource->totalUnits = oldResource->totalUnits;
		resource->unitsLeft = oldResource->unitsLeft;
		resource->unitsOnHold = oldResource->unitsOnHold;
		newManager->resources[i] = resource;
	}

	newManager->queue = (Queue*) malloc(sizeof(Queue));
	for (i = 0; i < manager->queue->end; i++) {
		newManager->queue->blocked[i] = manager->queue->blocked[i];
	}
	newManager->queue->start = manager->queue->start;
	newManager->queue->end = manager->queue->end;

	return newManager;
}

void freeEverything(Manager* manager) {
	int i, j;

	for (i = 1; i <= manager->numTasks; i++) {
		for (j = 1; j <= manager->tasks[i]->numClaims; j++) {
			free(manager->tasks[i]->claims[j]);	
		}
		for (j = 0; j < manager->tasks[i]->numInstructions; j++) {
			free(manager->tasks[i]->instructions[j]);
		}
		free(manager->tasks[i]);
	}
	for (i = 1; i < manager->numResources; i++) {
		free(manager->resources[i]);
	}
	free(manager->queue);
	free(manager);
}

/*****************************************************************************/

int main(int argc, char* argv[]) {
  if (argc != 2) {
    printf("Enter the name of the input file as an argument.\n");
    exit(1);
  }

  Manager* optimisticManager = readFileInput(argv[1]);
	optimistic(optimisticManager);
	printOptimistic(optimisticManager);
	freeEverything(optimisticManager);	

	Manager* bankerManager = readFileInput(argv[1]);
  return 0;
}
