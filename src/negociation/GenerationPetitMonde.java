package negociation;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.Random;

/**
   This example shows a minimal agent that just prints "Hello World!"
   and then terminates.
   @author Giovanni Caire - TILAB
 */
public class GenerationPetitMonde extends Agent {
  protected int k = 4;
  protected int n = 10;
  protected double B = 0.2;
  protected Neighbour[] agents;
  static protected Random r;

  protected void connect(Neighbour n1, Neighbour n2) {
    System.out.println("connect " + n1 + " " + n2);
    n1.addNeighbour(n2);
    n2.addNeighbour(n1);
  }

  protected void disconnect(Neighbour n1, Neighbour n2) {
    System.out.println("disconnect " + n1 + " " + n2);
    n1.removeNeighbour(n2);
    n2.removeNeighbour(n1);
  }


  protected void setup() {
    agents = new Neighbour[n];
    r = new Random();
    try {
      ContainerController cc;
      AgentController ac;
      cc = getContainerController();
      for(int i = 0; i < n; i++) {
        Neighbour agent = new Neighbour();
        ac = cc.acceptNewAgent("Neighbour" + i, agent);
        ac.start();
        agents[i] = agent;
      }

      for(int i = 0; i < n; i++) {
        for(int j = 1; j < Math.floor(k/2 + 1); j++) {
            connect(agents[i], agents[(i + j) % n]);
        }
      }

      for (int i = 0; i < n; i++) {
        for (int j = i+1; j < n; j++) {
          if (agents[i].hasNeighbour(agents[j]) && r.nextFloat() < B) {
            disconnect(agents[i], agents[j]);

            List<Neighbour> notConnectedAgents = new ArrayList<Neighbour>(Arrays.asList(agents));
            notConnectedAgents.removeAll(agents[i].getNeighbours());
            notConnectedAgents.remove(agents[i]);

            connect(agents[i], getRandomNeighbour(notConnectedAgents));
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

  public static Neighbour getRandomNeighbour(List<Neighbour> l) {
    int rnd = r.nextInt(l.size());
    return l.get(rnd);
  }


}
