package negociation;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.Random;

/**
   This example shows a minimal agent that just prints "Hello World!"
   and then terminates.
   @author Giovanni Caire - TILAB
 */
public class GenerationReseau extends Agent {
  protected int nbAgents = 10;
  protected double connexionTaux = 0.2;
  protected Neighbour[] agents;

  protected void setup() {
    Random r = new Random();
    agents = new Neighbour[nbAgents];

    try {
      ContainerController cc;
      AgentController ac;
      cc = getContainerController();
      for(int i = 0; i < nbAgents; i++) {
        Neighbour agent = new Neighbour();
        ac = cc.acceptNewAgent("Neighbour" + i, agent);
        ac.start();
        agents[i] = agent;
      }

      for(int i = 0; i < nbAgents; i++) {
        for(int j = i+1; j < nbAgents; j++) {
          if(r.nextFloat() < connexionTaux) {
            Neighbour n1 = agents[i];
            Neighbour n2 = agents[j];
            n1.addNeighbour(n2);
            n2.addNeighbour(n1);
          }
        }
      }

    } catch (Exception e) {
      System.out.println(e);
    }

    System.out.println("Hello World! I am "+getLocalName());
   	// Make this agent terminate
  	doDelete();
  }
}
