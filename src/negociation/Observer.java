package negociation;
import jade.core.*;
import jade.core.Agent;
import java.util.List;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

public class Observer extends Agent {

  protected List<Neighbour> agents;
  private Logger myLogger = Logger.getMyLogger(getClass().getName());

  private class ObserverBehaviour extends CyclicBehaviour {

    public ObserverBehaviour(Agent a) {
      super(a);
    }

    public void action() {
      ACLMessage  msg = myAgent.receive();
      if(msg != null){
        if(msg.getPerformative() == ACLMessage.REQUEST) {
          String content = msg.getContent();
          if ((content != null) && (content.indexOf("info") != -1)){
            sendAgentsInfo(msg);
          }
          else if ((content != null) && (content.indexOf("trade") != -1)){
            trade(msg);
          }
          else if ((content != null) && (content.indexOf("start") != -1)){
            start(msg);
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
    }

    public void sendAgentsInfo(ACLMessage msg) {
      String res = "";
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.INFORM);

      for (Neighbour n : agents) {
        res += n.getLocalName() + n.getInventory() + "\n";
      }

      reply.setContent(res);
      System.out.print(res);
      send(reply);
    }

    public void trade(ACLMessage msg) {
      ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
      reply.setContent("trade");

      for (Neighbour n : agents) {
        reply.addReceiver(n.getAID());
      }

      send(reply);
    }

    public void start(ACLMessage msg) {
      ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
      reply.setContent("start");

      for (Neighbour n : agents) {
        reply.addReceiver(n.getAID());
      }

      send(reply);
    }

  }


  public void setAgentsList(List<Neighbour> agents) {
    this.agents = agents;
  }


  protected void setup() {
    // Registration with the DF
    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("Observer");
    sd.setName(getName());
    sd.setOwnership("Cojez");
    dfd.setName(getAID());
    dfd.addServices(sd);
    try {
      DFService.register(this,dfd);
      ObserverBehaviour ObserverBehaviour = new  ObserverBehaviour(this);
      addBehaviour(ObserverBehaviour);
    } catch (FIPAException e) {
      myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
      doDelete();
    }
  }
}
