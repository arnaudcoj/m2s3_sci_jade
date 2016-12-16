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
@author Arnaud Cojez, based on Giovanni Caire's Hello World example
*/
public class Neighbour extends Agent {

  static protected Random r;

  private Logger myLogger = Logger.getMyLogger(getClass().getName());
  protected List<Neighbour> neighbours = new LinkedList<Neighbour>();

  protected int oranges;
  protected int tomatoes;
  protected int oranges_desired;
  protected int tomatoes_desired;

  private class TradingBehaviour extends SimpleBehaviour {

    protected int proposals;

    public TradingBehaviour(Agent a) {
      super(a);
    }

    public boolean done() {
      return (oranges == oranges_desired && tomatoes == tomatoes_desired) || proposals > 20;
    }
    //message handling and routing
    public void action() {
      ACLMessage  msg = myAgent.receive();
      if(msg != null){
        if(msg.getPerformative() == ACLMessage.PROPOSE) {
          proposals += 1;
          String content = msg.getContent();
          if ((content != null) && (content.indexOf("orange") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received ORANGE Proposal from "+msg.getSender().getLocalName());
            handleOrangeProposal(msg);
          }
          if ((content != null) && (content.indexOf("tomato") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received TOMATO Proposal from "+msg.getSender().getLocalName());
            handleTomatoProposal(msg);
          }
        }
        else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
          proposals -= 1;
          String content = msg.getContent();
          if ((content != null) && (content.indexOf("orange") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received ORANGE Accept from "+msg.getSender().getLocalName());
            handleOrangeAccept();
          }
          else if ((content != null) && (content.indexOf("tomato") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received TOMATO Accept from "+msg.getSender().getLocalName());
            handleTomatoAccept();
          }
        }
        else if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
          String content = msg.getContent();
          if ((content != null) && (content.indexOf("orange") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received ORANGE Reject from "+msg.getSender().getLocalName());
            handleOrangeRefuse();
          }
          else if ((content != null) && (content.indexOf("tomato") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received TOMATO Reject from "+msg.getSender().getLocalName());
            handleTomatoRefuse();
          }
        }
        else if(msg.getPerformative()== ACLMessage.REQUEST){
          String content = msg.getContent();
          /*if ((content != null) && (content.indexOf("trade") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received TRADE Request from "+msg.getSender().getLocalName());
            trade(msg);
          }
          else */
          if ((content != null) && (content.indexOf("list") != -1)) {
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received LIST Request from "+msg.getSender().getLocalName());
            listNeighbours(msg);
          }
          else if ((content != null) && (content.indexOf("inv") != -1)) {
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received INVENTORY Request from "+msg.getSender().getLocalName());
            inventory(msg);
          }
          else{
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected request ["+content+"] received from "+msg.getSender().getLocalName());

            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("( UnexpectedContent ("+content+"))");
            send(reply);
          }

        }
        else {
          myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected message ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());

          ACLMessage reply = msg.createReply();
          reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
          reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )");
          send(reply);
        }

      }
      else {
        block();
      }
      trade(msg);
    }

    //REQUESTS===========================

    protected void listNeighbours(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      String res = "";
      reply.setPerformative(ACLMessage.INFORM);

      for (Neighbour n : neighbours) {
        res += n.toString() + " ";
      }

      reply.setContent(res);
      send(reply);
    }

    protected void inventory(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      String res = "";
      reply.setPerformative(ACLMessage.INFORM);

      reply.setContent(myAgent.getLocalName() + " : Tomatoes = " + tomatoes + "/" +  tomatoes_desired + ", Oranges = " + oranges + "/" + oranges_desired);
      send(reply);
    }

    protected void trade(ACLMessage msg) {
      String res = "";
      for (int i = 0; i < neighbours.size(); i++) {
        Neighbour n = neighbours.get(i);

        if(oranges - i > oranges_desired) {
          ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
          reply.addReceiver(n.getAID());
          reply.setContent("orange");
          send(reply);
        }
        if(tomatoes - i > tomatoes_desired) {
          ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
          reply.addReceiver(n.getAID());
          reply.setContent("tomato");
          send(reply);
        }
      }

    }

    //TRADE=============================

    protected void handleOrangeProposal(ACLMessage msg) {
      if(oranges < oranges_desired) {
        acceptOrange(msg);
      } else {
        refuseOrange(msg);
      }
    }

    protected void acceptOrange(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
      reply.setContent("orange");
      oranges += 1;
      //send message
      send(reply);
    }

    protected void refuseOrange(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
      reply.setContent("orange");
      //send message
      send(reply);
    }


    protected void handleTomatoProposal(ACLMessage msg) {
      if(tomatoes < tomatoes_desired) {
        acceptTomato(msg);
      } else {
        refuseTomato(msg);
      }
    }

    protected void acceptTomato(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
      reply.setContent("tomato");
      tomatoes += 1;
      //send message
      send(reply);
    }

    protected void refuseTomato(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
      reply.setContent("tomato");
      //send message
      send(reply);
    }



    protected void handleOrangeAccept() {
      if(oranges < 1) {
        throw new IllegalStateException("oranges < 1 when accepting trade");
      } else {
        oranges -= 1;
      }
    }

    protected void handleTomatoAccept() {
      if(tomatoes < 1) {
        throw new IllegalStateException("tomatoes < 1 when accepting trade");
      } else {
        tomatoes -= 1;
      }
    }

    protected void handleOrangeRefuse() {

    }

    protected void handleTomatoRefuse() {

    }

  }

  //SETUP============================

  protected String getInventory() {
    return ", Oranges = " + oranges + "/" + oranges_desired + ", Tomatoes = " + tomatoes + "/" +  tomatoes_desired;
  }

  protected void setup() {
    r = new Random();
    oranges_desired = r.nextInt(10);
    tomatoes_desired = r.nextInt(10);
    oranges = r.nextInt(10);
    tomatoes = r.nextInt(10);
    //System.out.println("Hello World! My name is "+getLocalName() + " " + oranges + "/" + oranges_desired + " " + tomatoes + "/" + tomatoes_desired);

    // Registration with the DF
    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("Neighbour");
    sd.setName(getName());
    sd.setOwnership("Cojez");
    dfd.setName(getAID());
    dfd.addServices(sd);
    try {
      DFService.register(this,dfd);
      TradingBehaviour TradingBehaviour = new  TradingBehaviour(this);
      addBehaviour(TradingBehaviour);
    } catch (FIPAException e) {
      myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
      doDelete();
    }

    System.out.println(getLocalName() + getInventory());
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
