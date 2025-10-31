<%--
  Owner Details View - displays comprehensive owner information with pet and visit history
  
  Model Attributes Expected:
  - owner: Owner entity with fully populated pet collection and visit histories
  
  Key Features:
  - Owner contact information display with proper XSS protection via c:out
  - Action buttons for owner editing and pet registration
  - Comprehensive pet listing with visit history in tabular format
  - Accessible markup with proper ARIA attributes and table headers
  
  Security Considerations:
  - All dynamic content escaped via c:out or fn:escapeXml to prevent XSS
  - URL parameters properly encoded through Spring URL building
  - No session state required (session="false")
  
  Performance Notes:
  - Pet collection should be eagerly loaded to avoid N+1 queries
  - Visit histories loaded per pet - consider pagination for extensive histories
  - Custom date formatting handled through petclinic:localDate tag
  
  TODO: Add pagination support for owners with many pets
  TODO: Consider AJAX loading for visit histories to improve initial page load
--%>
<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="owners">

    <!-- Owner Contact Information Section -->
    <h2 id="ownerInformation">Owner Information</h2>

    <!-- Accessible data table with proper headers and XSS protection -->
    <table class="table table-striped" aria-describedby="ownerInformation">
        <tr>
            <th id="name">Name</th>
            <td headers="name"><strong><c:out value="${owner.firstName} ${owner.lastName}"/></strong></td>
        </tr>
        <tr>
            <th id="address">Address</th>
            <td headers="address"><c:out value="${owner.address}"/></td>
        </tr>
        <tr>
            <th id="city">City</th>
            <td headers="city"><c:out value="${owner.city}"/></td>
        </tr>
        <tr>
            <th id="telephone">Telephone</th>
            <td headers="telephone"><c:out value="${owner.telephone}"/></td>
        </tr>
    </table>

    <spring:url value="{ownerId}/edit" var="editUrl">
        <spring:param name="ownerId" value="${owner.id}"/>
    </spring:url>
    <a href="${fn:escapeXml(editUrl)}" class="btn btn-primary">Edit Owner</a>

    <spring:url value="{ownerId}/pets/new" var="addUrl">
        <spring:param name="ownerId" value="${owner.id}"/>
    </spring:url>
    <a href="${fn:escapeXml(addUrl)}" class="btn btn-primary">Add New Pet</a>

    <br/>
    <br/>
    <br/>
    <h2 id="petsAndVisits">Pets and Visits</h2>

    <table class="table table-striped" aria-describedby="petsAndVisits">
        <c:forEach var="pet" items="${owner.pets}">

            <tr>
                <th scope="col">
                    <dl class="dl-horizontal">
                        <dt>Name</dt>
                        <dd><c:out value="${pet.name}"/></dd>
                        <dt>Birth Date</dt>
                        <dd><petclinic:localDate date="${pet.birthDate}" pattern="yyyy-MM-dd"/></dd>
                        <dt>Type</dt>
                        <dd><c:out value="${pet.type.name}"/></dd>
                    </dl>
                </th>
                <td>
                    <table class="table-condensed" aria-describedby="petsAndVisits">
                        <thead>
                        <tr>
                            <th id="visitDate">Visit Date</th>
                            <th id="visitDescription">Description</th>
                        </tr>
                        </thead>
                        <c:forEach var="visit" items="${pet.visits}">
                            <tr>
                                <td headers="visitDate"><petclinic:localDate date="${visit.date}" pattern="yyyy-MM-dd"/></td>
                                <td headers="visitDescription"><c:out value="${visit.description}"/></td>
                            </tr>
                        </c:forEach>
                        <tr>
                            <td>
                                <spring:url value="/owners/{ownerId}/pets/{petId}/edit" var="petUrl">
                                    <spring:param name="ownerId" value="${owner.id}"/>
                                    <spring:param name="petId" value="${pet.id}"/>
                                </spring:url>
                                <a href="${fn:escapeXml(petUrl)}">Edit Pet</a>
                            </td>
                            <td>
                                <spring:url value="/owners/{ownerId}/pets/{petId}/visits/new" var="visitUrl">
                                    <spring:param name="ownerId" value="${owner.id}"/>
                                    <spring:param name="petId" value="${pet.id}"/>
                                </spring:url>
                                <a href="${fn:escapeXml(visitUrl)}">Add Visit</a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

        </c:forEach>
    </table>

</petclinic:layout>
