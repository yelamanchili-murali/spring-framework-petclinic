<%--
  Master Layout Tag - Provides consistent page structure for all PetClinic pages
  
  Purpose:
  - Defines common HTML document structure with responsive Bootstrap layout
  - Includes standard header, navigation, footer components
  - Supports custom JavaScript injection via fragment
  - Maintains consistent look and feel across application
  
  Tag Attributes:
  - pageName: Required. Used for navigation highlighting and page identification
  - customScript: Optional fragment for page-specific JavaScript code
  
  Component Structure:
  - htmlHeader: Meta tags, CSS includes, page title generation
  - bodyHeader: Navigation menu with active page highlighting
  - Main content area: Bootstrap container-fluid with responsive grid
  - pivotal: Sponsorship/branding section
  - footer: Standard application footer
  
  Usage Pattern:
  - Wrap page content in <petclinic:layout pageName="sectionName">
  - Use pageName values: "home", "owners", "vets" for proper navigation
  - Include custom scripts using customScript fragment when needed
  
  Performance Notes:
  - CSS/JS resources loaded through htmlHeader tag for caching efficiency
  - Responsive design reduces mobile bandwidth requirements
  - Component reuse minimizes code duplication across pages
--%>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<%@ attribute name="pageName" required="true" %>
<%@ attribute name="customScript" required="false" fragment="true"%>

<!doctype html>
<html>
<petclinic:htmlHeader/>

<body>
<petclinic:bodyHeader menuName="${pageName}"/>

<div class="container-fluid">
    <div class="container xd-container">

        <jsp:doBody/>

        <petclinic:pivotal/>
    </div>
</div>
<petclinic:footer/>
<jsp:invoke fragment="customScript" />

</body>

</html>
