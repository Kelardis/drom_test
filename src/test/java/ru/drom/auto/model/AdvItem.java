package ru.drom.auto.model;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Getter
public class AdvItem {
    private String title;
    private int year;
    private boolean isRemoved;
    private boolean isHybrid;
    private String mileage;
    //...
    //other fields

    /**
     * Extracts fields from b-advItem
     *
     * @param element - b-advItem element of filtered results
     */
    public AdvItem(WebElement element) {
        this.isRemoved = element.getAttribute("class").contains("b-advItem_removed");
        parseSectionTypeMain(element.findElement(By.xpath(".//div[contains(@class, 'b-advItem__section_type_main')]")));
        parseSectionTypeParams(element.findElement(By.xpath(".//div[contains(@class, 'b-advItem__section_type_params')]")));

    }

    private void parseSectionTypeMain(WebElement element) {
        String[] combinedTitle = element.findElement(By.xpath(".//div[@class='b-advItem__title']")).getText().split(",");
        this.title = combinedTitle[0];
        this.year = Integer.valueOf(combinedTitle[1].trim());
    }

    private void parseSectionTypeParams(WebElement element) {
        this.mileage = tryExtractTextByDataFtid(element, "sales__bulls-item_mileage");
        this.isHybrid = "гибрид".equals(tryExtractTextByDataFtid(element, "sales__bulls-item_is_hybrid"));
        //other fields
    }

    private String tryExtractTextByDataFtid(WebElement element, String dataFtid) {
        try {
            return element.findElement(By.xpath(".//div[@data-ftid='" + dataFtid + "']")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

}
