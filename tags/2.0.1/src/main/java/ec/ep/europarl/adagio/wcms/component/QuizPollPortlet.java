package ec.ep.europarl.adagio.wcms.component;


public class QuizPollPortlet extends AbstractPortletComponent {

	@Override
	public String getType() {
		return "quiz-poll-portlet";
	}

	@Override
	public String getPortletName() {
		return "QuizPoll";
	}
}
