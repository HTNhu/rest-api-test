package gss.workshop.testing.tests;

import gss.workshop.testing.pojo.board.BoardCreationRes;
import gss.workshop.testing.pojo.card.CardCreationRes;
import gss.workshop.testing.pojo.card.CardUpdateRes;
import gss.workshop.testing.pojo.list.ListCreationRes;
import gss.workshop.testing.requests.RequestFactory;
import gss.workshop.testing.utils.ConvertUtils;
import gss.workshop.testing.utils.OtherUtils;
import gss.workshop.testing.utils.ValidationUtils;
import io.restassured.response.Response;
import org.testng.annotations.Test;

public class TrelloTests extends TestBase {

  @Test
  public void trelloWorkflowTest() {
    // 1. Create new board without default list
    String boardName = OtherUtils.randomBoardName();
    Response resBoardCreation = RequestFactory.createBoard(boardName, false);

    // VP. Validate status code
    ValidationUtils.validateStatusCode(resBoardCreation, 200);

    // VP. Validate a board is created: Board name and permission level
    BoardCreationRes board =
        ConvertUtils.convertRestResponseToPojo(resBoardCreation, BoardCreationRes.class);
    ValidationUtils.validateStringEqual(boardName, board.getName());
    ValidationUtils.validateStringEqual("private", board.getPrefs().getPermissionLevel());

    // -> Store board Id
    String boardId = board.getId();
    System.out.println(String.format("Board Id of the new Board: %s", boardId));

    // 2. Create a TODO list
    Response resTODOListCreation = RequestFactory.createList(boardId, "TODO");
    // VP. Validate status code
    ValidationUtils.validateStatusCode(resTODOListCreation, 200);
    // VP. Validate a list is created: name of list, closed attribute
    ListCreationRes todoList = ConvertUtils.convertRestResponseToPojo(resTODOListCreation, ListCreationRes.class);
    ValidationUtils.validateStringEqual("TODO", todoList.getName());
    ValidationUtils.validateStringEqual(false, todoList.getClosed());
    // VP. Validate the list was created inside the board: board Id
    ValidationUtils.validateStringEqual(boardId, todoList.getIdBoard());
    // Store todoList Id
    String todoListId = todoList.getId();

    // 3. Create a DONE list
    Response resDONEListCreation = RequestFactory.createList(boardId, "DONE");
    // VP. Validate status code
    ValidationUtils.validateStatusCode(resDONEListCreation, 200);
    // VP. Validate a list is created: name of list, "closed" property
    ListCreationRes doneList = ConvertUtils.convertRestResponseToPojo(resTODOListCreation, ListCreationRes.class);
    ValidationUtils.validateStringEqual("TODO", doneList.getName());
    ValidationUtils.validateStringEqual(false, doneList.getClosed());
    // VP. Validate the list was created inside the board: board Id
    ValidationUtils.validateStringEqual(boardId, doneList.getIdBoard());
    // Store todoList Id
    String doneListId = doneList.getId();

    // 4. Create a new Card in TODO list
    String taskName = OtherUtils.randomTaskName();
    Response resCardCreation = RequestFactory.createCard(taskName, todoListId);
    // VP. Validate status code
    ValidationUtils.validateStatusCode(resDONEListCreation, 200);
    // VP. Validate a card is created: task name, list id, board id
    CardCreationRes card = ConvertUtils.convertRestResponseToPojo(resCardCreation, CardCreationRes.class);
    ValidationUtils.validateStringEqual(taskName, card.getName());
    ValidationUtils.validateStringEqual(todoListId, card.getIdList());
    ValidationUtils.validateStringEqual(boardId, card.getIdBoard());
    // VP. Validate the card should have no votes or attachments
    ValidationUtils.validateStringEqual(0, card.getBadges().getVotes());
    // Store the card Id
    String cardId = card.getId();

    // 5. Move the card to DONE list
    Response resCardUpdate = RequestFactory.updateCard(cardId, doneListId);
    // VP. Validate status code
    ValidationUtils.validateStatusCode(resCardUpdate, 200);
    // VP. Validate the card should have new list: list id
    CardUpdateRes updateCard = ConvertUtils.convertRestResponseToPojo(resCardUpdate, CardUpdateRes.class);
    ValidationUtils.validateStringEqual(doneListId, updateCard.getIdList());
    // VP. Validate the card should preserve properties: name task, board Id, "closed" property
    ValidationUtils.validateStringEqual(taskName, updateCard.getName());
    ValidationUtils.validateStringEqual(boardId, updateCard.getIdBoard());
    ValidationUtils.validateStringEqual(false, updateCard.getClosed());

    // 6. Delete board
    Response resDeleteBoard = RequestFactory.deleteBoard(boardId);
    // VP. Validate status code
    ValidationUtils.validateStatusCode(resDeleteBoard, 200);
  }
}
