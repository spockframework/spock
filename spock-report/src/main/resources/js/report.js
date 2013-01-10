$(document).ready(function() {
  // expand/collapse tree elements
  $(".elementHeader").click(function() {
    toggleElements($(this).closest(".element"));
  });
  
  // expand/collapse narrative
  $(".narrative").click(function() {
    toggleNarratives($(this).closest(".element"));
  });
  
  // show/hide results
  $("#showResults").click(function() {
    $(".results").toggle();
  });

  // set initial collapsed state
  collapseElements($(".element"));
  collapseNarratives($(".specElement"));
        
  drawPieCharts();
  
  filterByState();

  performExpansions();
});

function drawPieCharts() {
  $("h1 span.pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 30
  });
  $("h2 span.pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 25
  });
  $("h3 span.pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 20
   });
}

function filterByState() {
  $("#state-all").click(function() {
    $(".specElement").show();
    $(".spec.passed .elementHeader .label").addClass("label-success");
    $(".spec.failed .elementHeader .label").addClass("label-important");
    $(".spec.skipped .elementHeader .label").addClass("label-warning");
  });
  $("#state-passed").click(function() {
    showSpecElementsContainingState("passed");
    $(".spec.passed .elementHeader .label").addClass("label-success");
    $(".spec.failed .elementHeader .label").removeClass("label-important");
  });
  $("#state-failed").click(function() {
    showSpecElementsContainingState("failed");
    $(".spec.passed .elementHeader .label").removeClass("label-success");
    $(".spec.failed .elementHeader .label").addClass("label-important");
  });
  $("#state-skipped").click(function() {
    showSpecElementsContainingState("skipped");
    $(".spec.passed .elementHeader .label").removeClass("label-success");
    $(".spec.failed .elementHeader .label").removeClass("label-important");
  });
}

function performExpansions() {
  $("#expansions-packages").click(function() {
    collapseElements($(".element"));
    collapseNarratives($(".specElement"));
  });

  $("#expansions-specifications").click(function() {
    expandElements($(".package"));
    collapseElements($(".element:not(.package)"));
    collapseNarratives($(".specElement"));
  });

  $("#expansions-requirements").click(function() {
    expandElements($(".package, .spec"));
    collapseElements($(".element:not(.package):not(.spec)"));
    collapseNarratives($(".specElement"));
  });

  $("#expansions-exceptions").click(function() {
    expandElements($(".specElement:has(.exceptions)"));
    expandElements($(".element.exceptions"));
    collapseElements($(".element:not(.exceptions):not(:has(.exceptions))"));
    collapseNarratives($(".specElement"));
  });

  $("#expansions-narrative").click(function() {
    expandElements($(".specElement:has(.narrative)"));
    collapseElements($(".element:not(:has(.narrative))"));
    expandNarratives($(".specElement"));
  });

  $("#expansions-full-details").click(function() {
    expandElements($(".element"));
    expandNarratives($(".specElement"));
  });
}

// TODO: do more work in CSS selectors
function showSpecElementsContainingState(state) {
  $(".specElement").each(function() {
    $(this).toggle($(this).hasClass(state) || $(this).find(".specElement." + state).length > 0);
  });
}

function expandElements(elements) {
  elements.removeClass("collapsed");
  elements.children(".elementBody").show();
}

function collapseElements(elements) {
  elements.addClass("collapsed");
  elements.children(".elementBody").hide();
}

function toggleElements(elements) {
  elements.toggleClass("collapsed");
  elements.children(".elementBody").toggle();
}

function expandNarratives(elements) {
  elements.find("> .elementBody > .narrative:not(.collapsed)").show();
  elements.find("> .elementBody > .narrative.collapsed").hide();
}

function collapseNarratives(elements) {
  elements.find("> .elementBody > .narrative:not(.collapsed)").hide();
  elements.find("> .elementBody > .narrative.collapsed").show();
}

function toggleNarratives(elements) {
  elements.find("> .elementBody > .narrative").toggle();
}

