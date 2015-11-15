#include <stdio.h>
#include <stdlib.h>

#define MAX 100

typedef struct Claim {
  int id;
  int claimUnits;
} Claim;

typedef enum Action {
	INITIATE, REQUEST, RELEASE, COMPUTE, TERMINATE;
} Action;

typedef struct Instruction {
	Action action;
	int thirdNumber;  // Resource type or number of cycles
	int fourthNumber; // Initial claim, number requested, or number released
} Instruction;

typedef struct Task {
  int id;
  int numClaims;
  Claim claim[MAX];
  int cycleTerminated;
  int cyclesWaiting;
	Instruction instructions[MAX];
	int numInstructions;
} Task;

typedef struct Resource {
  int id;
  int totalUnits;
} Resource;

typedef struct Manager {
  Task tasks[MAX];
  Resource resources[MAX];
  int cycle;
  int numTasks;
  int numResources;
} Manager;

Manager* readFileInput(char* fileName) {
  int i = 0;

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
  manager->cycle = 0;

  // Create resources and tasks
  int temp = 1;
  while (temp <= manager->numResources) {
    // Create single resource
    Resource* resource = (Resource*) malloc(sizeof(Resource));
    resource->id = temp;
    fscanf(file, "%d", &resource->totalUnits);
    manager->resources[temp] = *resource;

    // Create single task
    Task* task = (Task*) malloc(sizeof(Task));
    task->id = temp;
    manager->tasks[temp] = *task;
    temp++;
  }

	readRestOfFile(file, manager);
  return manager;
}

void readRestOfFile(FILE file, Manager* manager) {
	char* action;
	int i, j, k;

	while (fscanf(file, "%s %d %d %d", &action, &i, &j, &k) > 0) {
		Instruction instruction = (Instruction) malloc(sizeof(Instruction));
		instruction.thirdNumber = j;
		instruction.fourthNumber = k;

		switch (action) {
			case "initiate": instruction.action = Action.INITIATE; break;
			case "request": instruction.action = Action.REQUEST; break;
			case "release": instruction.actions = Action.RELEASE; break;
			case "compute": instruction.actions = Action.COMPUTE; break;
			case "terminate": instruction.actions = Action.TERMINATE; break;
			default: printf("Action not found...\n");
		}
		manager->tasks[i].instructions[numInstructions] = instruction;
	}
}

int main(int argc, char* argv[]) {

  if (argc != 2) {
    printf("You must enter the name of the file containing the input as an argument.\n");
    exit(1);
  }

  Manager* manager = readFileInput(argv[1]);
  return 0;
}
