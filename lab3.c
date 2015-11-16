#include <stdio.h>
#include <stdlib.h>

#define MAX 100

typedef struct Claim {
  int id;
  int claimUnits;
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

typedef struct Resource {
  int id;
  int totalUnits;
	int unitsLeft;
	int unitsOnHold;
} Resource;

typedef struct Manager {
  Task* tasks[MAX];
  Resource* resources[MAX];
  int cycle;
  int numTasks;
  int numResources;
} Manager;

/* Function Declarations */
void readRestOfFile(FILE* file, Manager* manager);

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
  fscanf(file, "%d %d", &manager->numTasks, &manager->numResources);
  manager->cycle = 1;

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
		tasks->currentInstruction = 1;
    manager->tasks[temp] = task;
    temp++;
	}

	readRestOfFile(file, manager);
  return manager;
}

void readRestOfFile(FILE* file, Manager* manager) {
	char action[10];
	int i, j, k;
	
	while (fscanf(file, "%s%*[ ] %d %d %d", &action, &i, &j, &k) > 0) {
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
			printf("Action not found...\n");
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
		
		for (i = 1; i <= manager->numTasks; i++) {
			Task* t = manager->tasks[i];
			if (t->state == BLOCKED && canRequest(t, manager)) {
				t->state = UNBLOCKED;
				t->currentInstruction++;
			}
		}

		performInstructions(manager);

		for (i = 1; i <= manager->numTasks; i++) {
			Task* t = manager->tasks[i];
			if (t->state == UNBLOCKED) {
				t->state = NORMAL;
			}
		}


		if (deadlock(manager) && !allDone(manager)) {
			for (i = 1; i <= manager->numTasks; i++) {
				if (manager->tasks[i].state == BLOCKED) {
					Task* toAbort = manager->tasks[i];
					for (j = 1; j <= toAbort->currentInstruction; j++) {
						Instruction* currentInstruct = toAbort->instructions[j];
						if (currentInstruct.state == REQUEST) {
							Resource* r = manager->resources[currentInstruct->thirdNumber];
							r->unitsLeft += currentInstruct->fourthNumber;
						}
					}
					toAbort->state = ABORTED;
					if (enough(manager)) {
						break;
					}
				}
			}
		}

		// Make units on hold released from this cycle available for next round
		for (i = 1; i <= manager->numResources; i++) {
			Resource* r = manager->resources[i];
			r->unitsLeft += r->unitsOnHold;
			r->unitsOnHold = 0;
		}
		
		manager->cycle++;		
	}		
}

bool enough(Manager* manager) {
	int i;
	for (i = 0; i < manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == BLOCKED) {
			Instruction ins = t->instructions[currentInstruction];
			Resource* r = manager->resources[ins->thirdNumber];
			if (ins.state != REQUEST) {
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

void performInstructions(Manager* manager) {
	for (i = 1; i <= manager->numTasks; i++) {
		Task* t = manager->tasks[i];
		if (t->state == NORMAL) {
			Instruction* instruct = t->instructions[currentInstruction];
			switch (instruct->action) {
				case INITIATE: 
					break;
				case REQUEST: 
					if (canRequest(manager, t)) {
						t->currentInstruction++;
					} else {
						t-state = BLOCKED;
					}
					break;
				case RELEASE:
					Resource* r = manager->resources[instruct->thirdNumber];
					r->unitsOnHold += instruct->fourthNumber;
					t->currentInstruction++;
					break;
				case COMPUTE:
					instruct->thirdNumber--;
					if (instruct->thirdNumber <= 0) {
						t->state = NORMAL;
						t->currentInstruction++;
					}
					break;
				case TERMINATE:
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
		if (m->tasks[i].state != BLOCKED && m->tasks[i].state != TERMINATED
				&& m->tasks[i].state != ABORTED) {
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
		if (manager->tasks[i]->state != ABORTED && manager->tasks[i].state != TERMINATED) {
			return false;
		}
	}
	return true;
}

int main(int argc, char* argv[]) {
  if (argc != 2) {
    printf("You must enter the name of the file containing the input as an argument.\n");
    exit(1);
  }

  Manager* manager = readFileInput(argv[1]);
	int i;
	for (i = 1; i <= manager->numTasks; i++) {
		printf("%d\n", manager->tasks[i]->numInstructions);
	}

  return 0;
}
