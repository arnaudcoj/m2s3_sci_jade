package negociation;
import jade.core.*;
import jade.core.Agent;
import java.util.List;
import java.util.LinkedList;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

import java.util.Random;
/**
   This example shows a minimal agent that just prints "Hello World!"
   and then terminates.
   @author Giovanni Caire - TILAB
 */
public class Neighbour extends Agent {

  static protected Random r;

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
  protected List<Neighbour> neighbours = new LinkedList<Neighbour>();

  protected int oranges;
  protected int tomatoes;
  protected int oranges_desired;
  protected int tomatoes_desired;

	private class WaitPingAndReplyBehaviour extends CyclicBehaviour {

		public WaitPingAndReplyBehaviour(Agent a) {
			super(a);
		}

    public void action() {
  		ACLMessage  msg = myAgent.receive();
  		if(msg != null){
  			ACLMessage reply = msg.createReply();

  			if(msg.getPerformative()== ACLMessage.REQUEST){
  				String content = msg.getContent();
  				if ((content != null) && (content.indexOf("com") != -1)){
  					myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received COMMUNICATE Request from "+msg.getSender().getLocalName());
  					reply.setPerformative(ACLMessage.INFORM);
  					reply.setContent(communicate());
  				}
          else if ((content != null) && (content.indexOf("list") != -1)) {
  					myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received LIST Request from "+msg.getSender().getLocalName());
  					reply.setPerformative(ACLMessage.INFORM);
  					reply.setContent(listNeighbours());
          }
          else if ((content != null) && (content.indexOf("inv") != -1)) {
  					myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received INVENTORY Request from "+msg.getSender().getLocalName());
  					reply.setPerformative(ACLMessage.INFORM);
  					reply.setContent(tomatoes + " " + oranges);
          }
  				else{
  					myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected request ["+content+"] received from "+msg.getSender().getLocalName());
  					reply.setPerformative(ACLMessage.REFUSE);
  					reply.setContent("( UnexpectedContent ("+content+"))");
  				}

  			}
  			else {
  				myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected message ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());
  				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
  				reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )");
  			}
  			send(reply);
  		}
  		else {
  			block();
  		}
  	}
  }

  protected void setup() {
    r = new Random();
    oranges_desired = r.nextInt(10);
    tomatoes_desired = r.nextInt(10);
    oranges = r.nextInt(10);
    tomatoes = r.nextInt(10);
    System.out.println("Hello World! My name is "+getLocalName() + " " + oranges + "/" + oranges_desired + " " + tomatoes + "/" + tomatoes_desired);

		// Registration with the DF
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Neighbour");
		sd.setName(getName());
		sd.setOwnership("TILAB");
		dfd.setName(getAID());
		dfd.addServices(sd);
		try {
			DFService.register(this,dfd);
			WaitPingAndReplyBehaviour PingBehaviour = new  WaitPingAndReplyBehaviour(this);
			addBehaviour(PingBehaviour);
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
			doDelete();
		}
  }

  public String communicate() {
    String res = "";
    for (Neighbour n : neighbours) {
      res += n.toString() + " ";
    }
    return res;
  }

  public String listNeighbours() {
    String res = "";
    for (Neighbour n : neighbours) {
      res += n.toString() + " ";
    }
    return res;
  }

  public String toString() {
    return getLocalName();
  }

  protected void addNeighbour(Neighbour n) {
    this.neighbours.add(n);
  }

  protected void removeNeighbour(Neighbour n) {
    this.neighbours.remove(n);
  }

  protected boolean hasNeighbour(Neighbour n) {
    return this.neighbours.contains(n);
  }

  public List<Neighbour> getNeighbours() {
    return neighbours;
  }
}
