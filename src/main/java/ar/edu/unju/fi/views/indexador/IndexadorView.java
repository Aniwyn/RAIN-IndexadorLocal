package ar.edu.unju.fi.views.indexador;

import ar.edu.unju.fi.data.entity.ResultCard;
import ar.edu.unju.fi.lucene.Lucene;
import ar.edu.unju.fi.lucene.LuceneConstant;
import ar.edu.unju.fi.lucene.Observer;
import ar.edu.unju.fi.lucene.TextFileFilter;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import java.io.*;
import java.util.ArrayList;

@PageTitle("Indexador")
@Route(value = "")
@Uses(Icon.class)
public class IndexadorView extends Composite<VerticalLayout> {

    Lucene lucene;
    TextFileFilter textFileFilter = new TextFileFilter();
    private final TabSheet tabSheet = new TabSheet();
    Grid<ResultCard> grid = new Grid<>(ResultCard.class, false);
    CheckboxGroup<String> fileType = new CheckboxGroup<>("Tipo de archivos", "PDF", "DOCX", "TXT");
    MessageInput pathBar = new MessageInput();
    Button primaryButton = new Button("Aplicar");
    Observer observer;
    Thread observerThread;

    public IndexadorView() {
        lucene = new Lucene(textFileFilter);
        HorizontalLayout header = new HorizontalLayout();
        H1 h1 = new H1();
        Span badge = new Span();
        MessageInput searchBar = new MessageInput();

        getContent().setWidthFull();
        getContent().setSpacing(false);
        getContent().setHeightFull();

        header.addClassName(Gap.MEDIUM);
        header.addClassName(Padding.XSMALL);
        header.setWidthFull();
        h1.setText("Aniwyn Searcher");
        badge.setText("1.0.0");
        badge.getElement().getThemeList().add("badge");

        setSearchBar(searchBar);

        header.setFlexGrow(1.0, searchBar);
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        getContent().setFlexGrow(1.0, content);
        content.setFlexGrow(1.0, tabSheet);
        tabSheet.setWidthFull();
        setTabSheetSampleData(tabSheet);
        getContent().add(header);

        header.add(h1);
        header.add(badge);
        header.add(searchBar);
        getContent().add(content);
        content.add(tabSheet);

        searchBar.addSubmitListener(this::search);
        pathBar.addSubmitListener(this::setConfigPath);
        primaryButton.addClickListener(clickEvent -> setConfigTypeFile());

        observer = new Observer(lucene);
        observerThread = new Thread(observer);
        observerThread.start();
    }

    private void search(MessageInput.SubmitEvent submitEvent) {
        grid.removeAllColumns();
        ArrayList<ResultCard> resultCards;
        String query = submitEvent.getValue();
        tabSheet.getTabAt(0).setLabel("Resultados: " + query);
        tabSheet.setSelectedTab(tabSheet.getTabAt(0));
        try {
            resultCards = lucene.search(query);
        } catch (IOException | InvalidTokenOffsetsException | ParseException e) {
            throw new RuntimeException(e);
        }

        if (resultCards.isEmpty()) {
            ResultCard resultCard = new ResultCard();
            resultCard.setName("No se han encontrado coincidencias.");
            resultCard.setPath("");
            resultCard.setImage("images/no-resultados.png");
            resultCards.add(resultCard);
        }
        ListDataProvider<ResultCard> dataProvider = new ListDataProvider<>(resultCards);
        grid.setItems(dataProvider);
        grid.addComponentColumn(this::createCard);
    }

    private void setSearchBar(MessageInput searchBar) {
        MessageInputI18n messageInput = new MessageInputI18n();
        messageInput.setMessage("Busqueda...");
        messageInput.setSend("Buscar");
        searchBar.setI18n(messageInput);
    }

    private void setTabSheetSampleData(TabSheet tabSheet) {
        tabSheet.add("Resultados", resultSheet());
        tabSheet.add("Configuraciones", configSheet());
    }

    private Div resultSheet() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        return new Div(
                grid
        );
    }

    private Div configSheet() {
        getContent().setWidthFull();
        VerticalLayout configContent = new VerticalLayout();
        H3 pathTitle = new H3("Dirección a indexar: ");


        setPathBar();
        configContent.add(pathTitle);
        fileType.select("PDF", "DOCX", "TXT");

        primaryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        primaryButton.getStyle().set("block", "none");

        return new Div(
                configContent,
                pathBar,
                fileType,
                primaryButton
        );
    }

    private void setConfigPath(MessageInput.SubmitEvent submitEvent) {
        String newPath = submitEvent.getValue();
        File filePath = new File(newPath);
        if (filePath.isDirectory()) {
            File file = new File(LuceneConstant.LAST_PATH);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(newPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            setPathBar();
            restartService();
            observerThread.interrupt();
            observerThread= new Thread(observer);
            observerThread.start();
        } else {
            Notification.show("Directorio ingresado invalido.",3000, Notification.Position.BOTTOM_CENTER);
        }
    }

    private void restartService() {
        try {
            lucene.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setConfigTypeFile() {
        textFileFilter.setPdf(fileType.isSelected("PDF"));
        textFileFilter.setDocx(fileType.isSelected("DOCX"));
        textFileFilter.setTxt(fileType.isSelected("TXT"));
        restartService();
    }

    private void setPathBar() {
        MessageInputI18n messageInput = new MessageInputI18n();
        String path = "";
        try (BufferedReader br = new BufferedReader(new FileReader(LuceneConstant.LAST_PATH))) {
            path = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageInput.setMessage(path);
        messageInput.setSend("Cambiar");
        pathBar.setI18n(messageInput);
    }

    private HorizontalLayout createCard(ResultCard resultCard) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Image image = new Image();
        image.setSrc(resultCard.getImage());
        VerticalLayout description = new VerticalLayout();
        description.addClassName("description");
        description.setSpacing(false);
        description.setPadding(false);

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");
        header.setSpacing(false);
        header.getThemeList().add("spacing-s");

        H3 name = new H3(resultCard.getName());
        name.addClassName("name");
        header.add(name);

        Span post = new Span(resultCard.getPath());
        post.addClassName("post");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("actions");
        actions.setSpacing(false);
        actions.getThemeList().add("spacing-s");

        description.add(header, post, actions);
        Button openButton = new Button("Abrir");
        openButton.addClassName("openButton");

        openButton.addClickListener(clickEvent -> {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("powershell.exe", "-Command", "start \"\"" + resultCard.getPath() + "\"\"");
            try {
                processBuilder.start();
            } catch (IOException e) {
                Notification.show("No se pudo abrir el archivo.",3000, Notification.Position.BOTTOM_CENTER);
            }
        });

        card.add(image, description);
        if (!resultCard.getName().equals("No se han encontrado coincidencias.")) {
            card.add(openButton);
        }
        return card;
    }

}